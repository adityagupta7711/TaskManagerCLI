import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

// Task class to represent individual tasks
class Task {
    private int id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDate deadline;
    private boolean completed;
    
    public enum Priority {
        LOW(1), MEDIUM(2), HIGH(3);
        
        private final int value;
        
        Priority(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    public Task(int id, String title, String description, Priority priority, LocalDate deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.completed = false;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    // Convert task to file format
    public String toFileString() {
        return id + "|" + title + "|" + description + "|" + priority + "|" + 
               (deadline != null ? deadline.toString() : "null") + "|" + completed;
    }
    
    // Create task from file format
    public static Task fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid task format in file");
        }
        
        int id = Integer.parseInt(parts[0]);
        String title = parts[1];
        String description = parts[2];
        Priority priority = Priority.valueOf(parts[3]);
        LocalDate deadline = parts[4].equals("null") ? null : LocalDate.parse(parts[4]);
        boolean completed = Boolean.parseBoolean(parts[5]);
        
        Task task = new Task(id, title, description, priority, deadline);
        task.setCompleted(completed);
        return task;
    }
    
    @Override
    public String toString() {
        String status = completed ? "✓" : "✗";
        String deadlineStr = deadline != null ? deadline.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "No deadline";
        return String.format("[%s] ID: %d | %s | Priority: %s | Deadline: %s%n    %s", 
                           status, id, title, priority, deadlineStr, description);
    }
}

// File manager for handling task persistence
class TaskFileManager {
    private static final String FILENAME = "tasks.txt";
    
    public static void saveTasks(ArrayList<Task> tasks) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME))) {
            for (Task task : tasks) {
                writer.write(task.toFileString());
                writer.newLine();
            }
        }
    }
    
    public static ArrayList<Task> loadTasks() throws IOException {
        ArrayList<Task> tasks = new ArrayList<>();
        File file = new File(FILENAME);
        
        if (!file.exists()) {
            return tasks; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    try {
                        tasks.add(Task.fromFileString(line));
                    } catch (Exception e) {
                        System.err.println("Error parsing task: " + line + " - " + e.getMessage());
                    }
                }
            }
        }
        
        return tasks;
    }
}

// Main Task Manager class
public class TaskManagerCLI {
    private ArrayList<Task> tasks;
    private HashMap<Integer, Task> taskMap;
    private Scanner scanner;
    private int nextId;
    
    public TaskManagerCLI() {
        this.tasks = new ArrayList<>();
        this.taskMap = new HashMap<>();
        this.scanner = new Scanner(System.in);
        this.nextId = 1;
        loadTasksFromFile();
    }
    
    private void loadTasksFromFile() {
        try {
            tasks = TaskFileManager.loadTasks();
            taskMap.clear();
            
            for (Task task : tasks) {
                taskMap.put(task.getId(), task);
                if (task.getId() >= nextId) {
                    nextId = task.getId() + 1;
                }
            }
            
            System.out.println("Loaded " + tasks.size() + " tasks from file.");
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            System.out.println("Starting with empty task list.");
        }
    }
    
    private void saveTasksToFile() {
        try {
            TaskFileManager.saveTasks(tasks);
            System.out.println("Tasks saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }
    
    public void run() {
        System.out.println("=== Task Manager CLI ===");
        System.out.println("Welcome to your personal task manager!");
        
        while (true) {
            displayMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1 -> addTask();
                case 2 -> viewTasks();
                case 3 -> editTask();
                case 4 -> deleteTask();
                case 5 -> markTaskComplete();
                case 6 -> viewTasksByPriority();
                case 7 -> viewTasksByDeadline();
                case 8 -> {
                    saveTasksToFile();
                    System.out.println("Thank you for using Task Manager CLI!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void displayMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("1. Add Task");
        System.out.println("2. View All Tasks");
        System.out.println("3. Edit Task");
        System.out.println("4. Delete Task");
        System.out.println("5. Mark Task Complete");
        System.out.println("6. View Tasks by Priority");
        System.out.println("7. View Tasks by Deadline");
        System.out.println("8. Save and Exit");
        System.out.print("Choose an option (1-8): ");
    }
    
    private int getMenuChoice() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void addTask() {
        System.out.println("\n--- Add New Task ---");
        
        System.out.print("Enter task title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Task title cannot be empty!");
            return;
        }
        
        System.out.print("Enter task description: ");
        String description = scanner.nextLine().trim();
        
        Task.Priority priority = getPriorityInput();
        LocalDate deadline = getDeadlineInput();
        
        Task newTask = new Task(nextId++, title, description, priority, deadline);
        tasks.add(newTask);
        taskMap.put(newTask.getId(), newTask);
        
        System.out.println("Task added successfully with ID: " + newTask.getId());
    }
    
    private Task.Priority getPriorityInput() {
        while (true) {
            System.out.print("Select priority (1-Low, 2-Medium, 3-High): ");
            try {
                int priorityChoice = Integer.parseInt(scanner.nextLine().trim());
                switch (priorityChoice) {
                    case 1 -> { return Task.Priority.LOW; }
                    case 2 -> { return Task.Priority.MEDIUM; }
                    case 3 -> { return Task.Priority.HIGH; }
                    default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    private LocalDate getDeadlineInput() {
        System.out.print("Enter deadline (YYYY-MM-DD) or press Enter for no deadline: ");
        String deadlineStr = scanner.nextLine().trim();
        
        if (deadlineStr.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(deadlineStr);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. No deadline set.");
            return null;
        }
    }
    
    private void viewTasks() {
        System.out.println("\n--- All Tasks ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        for (Task task : tasks) {
            System.out.println(task);
            System.out.println();
        }
        
        System.out.println("Total tasks: " + tasks.size());
    }
    
    private void editTask() {
        System.out.println("\n--- Edit Task ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks to edit.");
            return;
        }
        
        System.out.print("Enter task ID to edit: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Task task = taskMap.get(id);
            
            if (task == null) {
                System.out.println("Task with ID " + id + " not found.");
                return;
            }
            
            System.out.println("Current task: " + task.getTitle());
            System.out.print("Enter new title (or press Enter to keep current): ");
            String newTitle = scanner.nextLine().trim();
            if (!newTitle.isEmpty()) {
                task.setTitle(newTitle);
            }
            
            System.out.print("Enter new description (or press Enter to keep current): ");
            String newDescription = scanner.nextLine().trim();
            if (!newDescription.isEmpty()) {
                task.setDescription(newDescription);
            }
            
            System.out.print("Update priority? (y/n): ");
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                task.setPriority(getPriorityInput());
            }
            
            System.out.print("Update deadline? (y/n): ");
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                task.setDeadline(getDeadlineInput());
            }
            
            System.out.println("Task updated successfully!");
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid task ID.");
        }
    }
    
    private void deleteTask() {
        System.out.println("\n--- Delete Task ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks to delete.");
            return;
        }
        
        System.out.print("Enter task ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Task task = taskMap.get(id);
            
            if (task == null) {
                System.out.println("Task with ID " + id + " not found.");
                return;
            }
            
            System.out.println("Task to delete: " + task.getTitle());
            System.out.print("Are you sure? (y/n): ");
            
            if (scanner.nextLine().trim().toLowerCase().startsWith("y")) {
                tasks.remove(task);
                taskMap.remove(id);
                System.out.println("Task deleted successfully!");
            } else {
                System.out.println("Delete cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid task ID.");
        }
    }
    
    private void markTaskComplete() {
        System.out.println("\n--- Mark Task Complete ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks available.");
            return;
        }
        
        System.out.print("Enter task ID to mark complete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Task task = taskMap.get(id);
            
            if (task == null) {
                System.out.println("Task with ID " + id + " not found.");
                return;
            }
            
            task.setCompleted(!task.isCompleted());
            String status = task.isCompleted() ? "completed" : "incomplete";
            System.out.println("Task marked as " + status + "!");
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid task ID.");
        }
    }
    
    private void viewTasksByPriority() {
        System.out.println("\n--- Tasks by Priority ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        // Sort tasks by priority (High to Low)
        ArrayList<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> Integer.compare(t2.getPriority().getValue(), t1.getPriority().getValue()));
        
        for (Task task : sortedTasks) {
            System.out.println(task);
            System.out.println();
        }
    }
    
    private void viewTasksByDeadline() {
        System.out.println("\n--- Tasks by Deadline ---");
        
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        
        // Separate tasks with and without deadlines
        ArrayList<Task> tasksWithDeadlines = new ArrayList<>();
        ArrayList<Task> tasksWithoutDeadlines = new ArrayList<>();
        
        for (Task task : tasks) {
            if (task.getDeadline() != null) {
                tasksWithDeadlines.add(task);
            } else {
                tasksWithoutDeadlines.add(task);
            }
        }
        
        // Sort tasks with deadlines by date
        tasksWithDeadlines.sort(Comparator.comparing(Task::getDeadline));
        
        // Display tasks with deadlines first
        for (Task task : tasksWithDeadlines) {
            System.out.println(task);
            System.out.println();
        }
        
        // Then display tasks without deadlines
        if (!tasksWithoutDeadlines.isEmpty()) {
            System.out.println("--- Tasks without deadlines ---");
            for (Task task : tasksWithoutDeadlines) {
                System.out.println(task);
                System.out.println();
            }
        }
    }
    
    public static void main(String[] args) {
        TaskManagerCLI taskManager = new TaskManagerCLI();
        taskManager.run();
    }
}