package exersise4;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SubwaySystem {
    private Map<String, Map<String, Double>> graph = new HashMap<>();
    private Map<String, List<String>> lines = new HashMap<>();
    private Scanner scanner = new Scanner(System.in);

    public SubwaySystem(String filename) throws FileNotFoundException {
        readData(filename);
    }

    private void readData(String filename) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filename));
        String currentLine = null;

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            if (line.contains("号线")) {
                currentLine = line.trim();
                lines.putIfAbsent(currentLine, new ArrayList<>());
            } else if (line.contains("---")) {
                String[] parts = line.split("\t");
                String[] stations = parts[0].split("---");
                Double distance = Double.parseDouble(parts[1]);
                String station1 = stations[0].trim();
                String station2 = stations[1].trim();
                addEdge(station1, station2, distance);
                addEdge(station2, station1, distance);
                lines.get(currentLine).add(station1);
                lines.get(currentLine).add(station2);
            }
        }
        fileScanner.close();
    }

    private void addEdge(String from, String to, double distance) {
        graph.putIfAbsent(from, new HashMap<>());
        graph.get(from).put(to, distance);
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n武汉地铁模拟系统");
            System.out.println("1. 查询某一站点的所有线路");
            System.out.println("2. 查询某条线路的所有站点");
            System.out.println("3. 查找所有中转站");
            System.out.println("4. 查找两站之间的最短路径");
            System.out.println("5. 计算两站之间的票价");
            System.out.println("6. 退出");
            System.out.print("请选择功能（1-6）：");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    queryStationLines();
                    break;
                case 2:
                    queryLineStations();
                    break;
                case 3:
                    findTransferStations();
                    break;
                case 4:
                    findShortestPath();
                    break;
                case 5:
                    calculateFare();
                    break;
                case 6:
                    System.out.println("谢谢使用！");
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    private void queryStationLines() {
        System.out.print("请输入站点名称：");
        String station = scanner.nextLine().trim();
        Set<String> stationLines = new HashSet<>();

        for (String line : lines.keySet()) {
            if (lines.get(line).contains(station)) {
                stationLines.add(line);
            }
        }

        if (stationLines.isEmpty()) {
            System.out.println("未找到该站点的相关线路。");
        } else {
            System.out.println("站点 " + station + " 所在的线路：");
            for (String line : stationLines) {
                System.out.println(line);
            }
        }
    }

    private void queryLineStations() {
        System.out.print("请输入线路名称：");
        String line = scanner.nextLine().trim();

        if (lines.containsKey(line)) {
            System.out.println("线路 " + line + " 包含的站点：");
            for (String station : new HashSet<>(lines.get(line))) {
                System.out.println(station);
            }
        } else {
            System.out.println("未找到该线路。");
        }
    }

    private void findShortestPath() {
        System.out.print("请输入起点站：");
        String start = scanner.nextLine().trim();
        System.out.print("请输入终点站：");
        String end = scanner.nextLine().trim();

        if (!graph.containsKey(start) || !graph.containsKey(end)) {
            System.out.println("站点名称有误，请检查输入！");
            return;
        }

        List<String> path = dijkstra(start, end);
        if (path.isEmpty()) {
            System.out.println("未找到从 " + start + " 到 " + end + " 的路径。");
        } else {
            System.out.println("从 " + start + " 到 " + end + " 的最短路径：");
            for (String station : path) {
                System.out.print(station + (station.equals(end) ? "" : " -> "));
            }
            System.out.println();
        }
    }

    private void calculateFare() {
        System.out.print("请输入起点站：");
        String start = scanner.nextLine().trim();
        System.out.print("请输入终点站：");
        String end = scanner.nextLine().trim();

        if (!graph.containsKey(start) || !graph.containsKey(end)) {
            System.out.println("站点名称有误，请检查输入！");
            return;
        }

        List<String> path = dijkstra(start, end);
        if (path.isEmpty()) {
            System.out.println("未找到从 " + start + " 到 " + end + " 的路径。");
            return;
        }

        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += graph.get(path.get(i)).get(path.get(i + 1));
        }

        double fare = calculateFareFromDistance(totalDistance);
        System.out.println("从 " + start + " 到 " + end + " 的票价为：" + fare + " 元");
    }

    private void findTransferStations() {
        System.out.println("所有中转站：");

        for (String station : graph.keySet()) {
            if (linesContainMultiple(station)) {
                System.out.println(station + ": " + getStationLines(station));
            }
        }

        if (!hasTransferStations()) {
            System.out.println("未找到任何中转站。");
        }
    }

    private String getStationLines(String station) {
        StringBuilder linesBuilder = new StringBuilder();
        Set<String> stationLines = new HashSet<>();

        for (String line : lines.keySet()) {
            if (lines.get(line).contains(station)) {
                stationLines.add(line);
            }
        }

        for (String line : stationLines) {
            linesBuilder.append(line).append(", ");
        }

        return linesBuilder.substring(0, linesBuilder.length() - 2); // Remove the trailing comma and space
    }

    private boolean hasTransferStations() {
        for (String station : graph.keySet()) {
            if (linesContainMultiple(station)) {
                return true;
            }
        }
        return false;
    }

    private boolean linesContainMultiple(String station) {
        Set<String> stationLines = new HashSet<>();

        for (String line : lines.keySet()) {
            if (lines.get(line).contains(station)) {
                stationLines.add(line);
            }
        }

        return stationLines.size() > 1;
    }

    private double calculateFareFromDistance(double distance) {
        if (distance <= 6) {
            return 2.0;
        } else if (distance <= 12) {
            return 3.0;
        } else if (distance <= 24) {
            return 4.0;
        } else if (distance <= 36) {
            return 5.0;
        } else {
            return 6.0;
        }
    }

    private List<String> dijkstra(String start, String end) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> node.distance));
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : graph.keySet()) {
            distances.put(vertex, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.add(new Node(start, 0.0));

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            visited.add(node.station);
            if (node.station.equals(end)) break;
            for (Map.Entry<String, Double> neighbor : graph.get(node.station).entrySet()) {
                if (visited.contains(neighbor.getKey())) continue;
                double newDist = distances.get(node.station) + neighbor.getValue();
                if (newDist < distances.get(neighbor.getKey())) {
                    distances.put(neighbor.getKey(), newDist);
                    previous.put(neighbor.getKey(), node.station);
                    queue.add(new Node(neighbor.getKey(), newDist));
                }
            }
        }

        List<String> path = new ArrayList<>();
        for (String at = end; at != null; at = previous.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return path.isEmpty() || !path.get(0).equals(start) ? new ArrayList<>() : path;
    }

    static class Node {
        String station;
        double distance;

        public Node(String station, double distance) {
            this.station = station;
            this.distance = distance;
        }
    }

    public static void main(String[] args) {
        try {
            SubwaySystem system = new SubwaySystem("C:/Users/caoyicheng/Desktop/subway.txt/");
            system.displayMenu();
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到：" + e.getMessage());
        }
    }
}