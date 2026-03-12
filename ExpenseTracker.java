import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Console-based personal expense tracker demonstrating data structures
 * (linked list, stack, queue, array) and algorithms (searching, sorting).
 * The application is menu-driven and uses Scanner with validation to avoid
 * runtime exceptions. All expenses are stored in a custom linked list.
 */
public class ExpenseTracker {
    private CustomLinkedList expenses;
    private CustomStack undoStack;
    private CustomQueue pendingExpenses;
    private int nextId;
    private Scanner scanner;

    // Remember how the list is currently sorted so that binary search can be
    // performed safely. Each flag corresponds to sort order of the last
    // operation; adding/removing items invalidates the flags.
    private boolean sortedByAmount;
    private boolean sortedByDate;
    private boolean sortedByCategory;

    public ExpenseTracker() {
        expenses = new CustomLinkedList();
        undoStack = new CustomStack(100);
        pendingExpenses = new CustomQueue(100);
        nextId = 1;
        scanner = new Scanner(System.in);

        // empty list is trivially "sorted"
        sortedByAmount = sortedByDate = sortedByCategory = true;
    }

    /* ---------------------------------------------------------------------
     * Input helpers
     * ---------------------------------------------------------------------
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = scanner.nextInt();
                scanner.nextLine(); // consume newline
                return val;
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a valid integer.");
                scanner.nextLine();
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double val = scanner.nextDouble();
                scanner.nextLine();
                return val;
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /* ---------------------------------------------------------------------
     * Basic operations
     * ---------------------------------------------------------------------
     */
    public void addExpense(String description, double amount, String category, String date) {
        Expense exp = new Expense(nextId++, description, amount, category, date);
        expenses.add(exp);
        undoStack.push(exp);
        // new element invalidates sorted order
        sortedByAmount = sortedByDate = sortedByCategory = false;
        System.out.println("Expense added successfully! ID: " + exp.getId());
    }

    public void addExpenseFromInput() {
        System.out.println("\n--- Add New Expense ---");
        String desc = readString("Enter description: ");
        double amt = readDouble("Enter amount: $");
        String cat = readString("Enter category: ");
        String date = readString("Enter date (YYYY-MM-DD): ");
        addExpense(desc, amt, cat, date);
    }

    public void addPendingExpense(String description, double amount, String category, String date) {
        Expense exp = new Expense(nextId++, description, amount, category, date);
        pendingExpenses.enqueue(exp);
        sortedByAmount = sortedByDate = sortedByCategory = false;
        System.out.println("Expense added to pending queue! ID: " + exp.getId());
    }

    public void addPendingExpenseFromInput() {
        System.out.println("\n--- Add Pending Expense ---");
        String desc = readString("Enter description: ");
        double amt = readDouble("Enter amount: $");
        String cat = readString("Enter category: ");
        String date = readString("Enter date (YYYY-MM-DD): ");
        addPendingExpense(desc, amt, cat, date);
    }

    public void processPendingExpenses() {
        if (pendingExpenses.isEmpty()) {
            System.out.println("No pending expenses to process.");
            return;
        }
        System.out.println("\n--- Processing Pending Expenses ---");
        while (!pendingExpenses.isEmpty()) {
            Expense exp = pendingExpenses.dequeue();
            expenses.add(exp);
            undoStack.push(exp);
            System.out.println("Processed: " + exp);
        }
        // new items added, sorted flags go false
        sortedByAmount = sortedByDate = sortedByCategory = false;
    }

    public void undoLastAdd() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        Expense last = undoStack.pop();
        boolean removed = expenses.removeById(last.getId());
        if (removed) {
            System.out.println("Undo successful: Removed expense - " + last);
            sortedByAmount = sortedByDate = sortedByCategory = false;
        } else {
            System.out.println("Undo failed: Expense not found.");
        }
    }

    /* ---------------------------------------------------------------------
     * Searching algorithms
     * ---------------------------------------------------------------------
     */
    // Linear search by ID - O(n)
    public void searchById(int id) {
        ExpenseNode cur = expenses.getHead();
        while (cur != null) {
            if (cur.expense.getId() == id) {
                System.out.println("Found: " + cur.expense);
                return;
            }
            cur = cur.next;
        }
        System.out.println("Expense with ID " + id + " not found.");
    }

    public void searchByIdFromInput() {
        int id = readInt("Enter ID to search: ");
        searchById(id);
    }

    // Linear search by category - O(n)
    public void searchByCategory(String category) {
        ExpenseNode cur = expenses.getHead();
        boolean found = false;
        System.out.println("\n--- Expenses in category: " + category + " ---");
        while (cur != null) {
            if (cur.expense.getCategory().equalsIgnoreCase(category)) {
                System.out.println(cur.expense);
                found = true;
            }
            cur = cur.next;
        }
        if (!found) {
            System.out.println("No expenses found in category: " + category);
        }
    }

    public void searchByCategoryFromInput() {
        String cat = readString("Enter category to search: ");
        searchByCategory(cat);
    }

    // Linear search by amount - O(n)
    public Expense linearSearchByAmount(double amount) {
        ExpenseNode cur = expenses.getHead();
        while (cur != null) {
            if (Double.compare(cur.expense.getAmount(), amount) == 0) {
                return cur.expense;
            }
            cur = cur.next;
        }
        return null;
    }

    // Binary search by amount - O(log n), requires sortedByAmount==true
    public Expense binarySearchByAmount(double amount) {
        if (!sortedByAmount) {
            System.out.println("Data not sorted by amount. Please sort first.");
            return null;
        }
        Expense[] array = expenses.toArray();
        int left = 0, right = array.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (Double.compare(array[mid].getAmount(), amount) == 0) {
                return array[mid];
            } else if (array[mid].getAmount() < amount) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    public void searchByAmountFromInput() {
        double amt = readDouble("Enter amount to search: $");
        System.out.print("Choose search type - 1) Linear  2) Binary: ");
        int choice = readInt("(1 or 2): ");
        Expense result = null;
        if (choice == 2) {
            result = binarySearchByAmount(amt);
        } else {
            result = linearSearchByAmount(amt);
        }
        if (result != null) {
            System.out.println("Found: " + result);
        } else {
            System.out.println("No expense with amount $" + amt + " found.");
        }
    }

    public void removeExpenseById() {
        int id = readInt("Enter ID of expense to remove: ");
        boolean removed = expenses.removeById(id);
        if (removed) {
            System.out.println("Expense with ID " + id + " removed successfully.");
            sortedByAmount = sortedByDate = sortedByCategory = false;
        } else {
            System.out.println("Expense with ID " + id + " not found.");
        }
    }

    /* ---------------------------------------------------------------------
     * Sorting algorithms
     * ---------------------------------------------------------------------
     */
    // Bubble sort on amount - O(n^2)
    public void sortByAmount() {
        if (expenses.size() <= 1) {
            System.out.println("Not enough expenses to sort.");
            return;
        }
        Expense[] array = expenses.toArray();
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (array[j].getAmount() > array[j + 1].getAmount()) {
                    Expense tmp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = tmp;
                }
            }
        }
        expenses.fromArray(array);
        sortedByAmount = true;
        sortedByDate = sortedByCategory = false;
        System.out.println("Expenses sorted by amount (ascending).");
    }

    // Selection sort on date - O(n^2)
    public void sortByDate() {
        if (expenses.size() <= 1) {
            System.out.println("Not enough expenses to sort.");
            return;
        }
        Expense[] array = expenses.toArray();
        for (int i = 0; i < array.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < array.length; j++) {
                if (array[j].getDate().compareTo(array[minIdx].getDate()) < 0) {
                    minIdx = j;
                }
            }
            Expense temp = array[i];
            array[i] = array[minIdx];
            array[minIdx] = temp;
        }
        expenses.fromArray(array);
        sortedByDate = true;
        sortedByAmount = sortedByCategory = false;
        System.out.println("Expenses sorted by date (ascending).");
    }

    // Insertion sort on category - O(n^2)
    public void sortByCategory() {
        if (expenses.size() <= 1) {
            System.out.println("Not enough expenses to sort.");
            return;
        }
        Expense[] array = expenses.toArray();
        for (int i = 1; i < array.length; i++) {
            Expense key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j].getCategory().compareTo(key.getCategory()) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
        expenses.fromArray(array);
        sortedByCategory = true;
        sortedByAmount = sortedByDate = false;
        System.out.println("Expenses sorted by category.");
    }

    // Merge sort and quick sort implementations omitted from menu but included
    // to demonstrate additional algorithms. They avoid printing inside recursion.

    private Expense[] mergeSort(Expense[] arr) {
        if (arr.length <= 1) return arr;
        int mid = arr.length / 2;
        Expense[] left = mergeSort(java.util.Arrays.copyOfRange(arr, 0, mid));
        Expense[] right = mergeSort(java.util.Arrays.copyOfRange(arr, mid, arr.length));
        return merge(left, right);
    }

    private Expense[] merge(Expense[] left, Expense[] right) {
        Expense[] result = new Expense[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) {
            if (left[i].getAmount() <= right[j].getAmount()) {
                result[k++] = left[i++];
            } else {
                result[k++] = right[j++];
            }
        }
        while (i < left.length) result[k++] = left[i++];
        while (j < right.length) result[k++] = right[j++];
        return result;
    }

    private void quickSort(Expense[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(Expense[] arr, int low, int high) {
        Expense pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j].getAmount() <= pivot.getAmount()) {
                i++;
                Expense temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        Expense temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;
        return i + 1;
    }

    /* ---------------------------------------------------------------------
     * Display & utility
     * ---------------------------------------------------------------------
     */
    public void displayAllExpenses() {
        System.out.println("\n--- All Expenses ---");
        expenses.display();
        System.out.printf("Total: $%.2f\n", getTotalExpenses());
    }

    public void displayPendingExpenses() {
        System.out.println("\n--- Pending Expenses ---");
        pendingExpenses.display();
    }

    private double getTotalExpenses() {
        double total = 0;
        ExpenseNode cur = expenses.getHead();
        while (cur != null) {
            total += cur.expense.getAmount();
            cur = cur.next;
        }
        return total;
    }

    private void displayMenu() {
        System.out.println("\n==================================");
        System.out.println("   EXPENSE TRACKER MENU");
        System.out.println("==================================");
        System.out.println("1. Add Regular Expense");
        System.out.println("2. Add Pending Expense");
        System.out.println("3. Process Pending Expenses");
        System.out.println("4. Display All Expenses");
        System.out.println("5. Display Pending Expenses");
        System.out.println("6. Search Expense by ID");
        System.out.println("7. Search Expenses by Category");
        System.out.println("8. Search Expenses by Amount");
        System.out.println("9. Remove Expense by ID");
        System.out.println("10. Sort Expenses by Amount");
        System.out.println("11. Sort Expenses by Date");
        System.out.println("12. Sort Expenses by Category");
        System.out.println("13. Undo Last Add");
        System.out.println("14. Show Total Expenses");
        System.out.println("15. Exit");
        System.out.println("==================================");
        System.out.print("Enter your choice (1-15): ");
    }

    public void run() {
        System.out.println("=== WELCOME TO EXPENSE TRACKER ===");
        System.out.println("Start by adding your first expense!");
        while (true) {
            displayMenu();
            int choice = readInt("" );
            switch (choice) {
                case 1 -> addExpenseFromInput();
                case 2 -> addPendingExpenseFromInput();
                case 3 -> processPendingExpenses();
                case 4 -> displayAllExpenses();
                case 5 -> displayPendingExpenses();
                case 6 -> searchByIdFromInput();
                case 7 -> searchByCategoryFromInput();
                case 8 -> searchByAmountFromInput();
                case 9 -> removeExpenseById();
                case 10 -> { sortByAmount(); displayAllExpenses(); }
                case 11 -> { sortByDate(); displayAllExpenses(); }
                case 12 -> { sortByCategory(); displayAllExpenses(); }
                case 13 -> undoLastAdd();
                case 14 -> System.out.printf("\nTotal Expenses: $%.2f\n", getTotalExpenses());
                case 15 -> {
                    System.out.println("Thank you for using Expense Tracker. Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        new ExpenseTracker().run();
    }

    /* ---------------------------------------------------------------------
     * Inner helper classes (nodes, list, stack, queue)
     * ---------------------------------------------------------------------
     */
    private static class ExpenseNode {
        Expense expense;
        ExpenseNode next;

        ExpenseNode(Expense expense) {
            this.expense = expense;
            this.next = null;
        }
    }

    private class CustomLinkedList {
        private ExpenseNode head;
        private int size;

        public CustomLinkedList() {
            head = null;
            size = 0;
        }

        public void add(Expense expense) {
            ExpenseNode newNode = new ExpenseNode(expense);
            if (head == null) {
                head = newNode;
            } else {
                ExpenseNode cur = head;
                while (cur.next != null) {
                    cur = cur.next;
                }
                cur.next = newNode;
            }
            size++;
        }

        public boolean removeById(int id) {
            if (head == null) return false;
            if (head.expense.getId() == id) {
                head = head.next;
                size--;
                return true;
            }
            ExpenseNode cur = head;
            while (cur.next != null) {
                if (cur.next.expense.getId() == id) {
                    cur.next = cur.next.next;
                    size--;
                    return true;
                }
                cur = cur.next;
            }
            return false;
        }

        public Expense get(int index) {
            if (index < 0 || index >= size) return null;
            ExpenseNode cur = head;
            for (int i = 0; i < index; i++) {
                cur = cur.next;
            }
            return cur.expense;
        }

        public int size() {
            return size;
        }

        public boolean isEmpty() {
            return head == null;
        }

        public void display() {
            if (head == null) {
                System.out.println("No expenses found.");
                return;
            }
            ExpenseNode cur = head;
            while (cur != null) {
                System.out.println(cur.expense);
                cur = cur.next;
            }
        }

        public Expense[] toArray() {
            Expense[] arr = new Expense[size];
            ExpenseNode cur = head;
            int idx = 0;
            while (cur != null) {
                arr[idx++] = cur.expense;
                cur = cur.next;
            }
            return arr;
        }

        public void fromArray(Expense[] arr) {
            head = null;
            size = 0;
            for (Expense e : arr) {
                add(e);
            }
        }

        public ExpenseNode getHead() {
            return head;
        }
    }

    private class CustomStack {
        private Expense[] stack;
        private int top;
        private int capacity;

        public CustomStack(int cap) {
            this.capacity = cap;
            this.stack = new Expense[cap];
            this.top = -1;
        }

        public void push(Expense exp) {
            if (top == capacity - 1) {
                System.out.println("Stack is full. Cannot push more expenses.");
                return;
            }
            stack[++top] = exp;
        }

        public Expense pop() {
            if (isEmpty()) {
                System.out.println("Stack is empty.");
                return null;
            }
            return stack[top--];
        }

        public Expense peek() {
            if (isEmpty()) return null;
            return stack[top];
        }

        public boolean isEmpty() {
            return top == -1;
        }

        public void display() {
            if (isEmpty()) {
                System.out.println("Stack is empty.");
                return;
            }
            System.out.println("Stack contents (top to bottom):");
            for (int i = top; i >= 0; i--) {
                System.out.println(stack[i]);
            }
        }
    }

    private class CustomQueue {
        private Expense[] queue;
        private int front;
        private int rear;
        private int size;
        private int capacity;

        public CustomQueue(int cap) {
            this.capacity = cap;
            this.queue = new Expense[cap];
            this.front = 0;
            this.rear = -1;
            this.size = 0;
        }

        public void enqueue(Expense exp) {
            if (isFull()) {
                System.out.println("Queue is full. Cannot enqueue more expenses.");
                return;
            }
            rear = (rear + 1) % capacity;
            queue[rear] = exp;
            size++;
        }

        public Expense dequeue() {
            if (isEmpty()) {
                System.out.println("Queue is empty.");
                return null;
            }
            Expense exp = queue[front];
            front = (front + 1) % capacity;
            size--;
            return exp;
        }

        public Expense peek() {
            if (isEmpty()) return null;
            return queue[front];
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public boolean isFull() {
            return size == capacity;
        }

        public void display() {
            if (isEmpty()) {
                System.out.println("Queue is empty.");
                return;
            }
            System.out.println("Queue contents (front to rear):");
            for (int i = 0; i < size; i++) {
                int idx = (front + i) % capacity;
                System.out.println(queue[idx]);
            }
        }
    }
}