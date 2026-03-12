import java.util.*;

class Expense
{
    String name;
    double amount;
    String date;

    Expense(String name,double amount,String date)
    {
        this.name=name;
        this.amount=amount;
        this.date=date;
    }
}

public class ExpenseTracker
{
    static Scanner sc=new Scanner(System.in);

    static String username="chandrika";
    static String password="1234";

    static double totalMoney=0;
    static double spent=0;

    static ArrayList<Expense> expenses=new ArrayList<>();

    public static void main(String args[])
    {

        System.out.println("===== PERSONAL EXPENSE TRACKER =====");

        login();

        System.out.print("Enter Total Money: ");
        totalMoney=sc.nextDouble();

        int choice;

        do
        {

            System.out.println("\n---- MENU ----");
            System.out.println("1. Add Expense");
            System.out.println("2. Delete Expense");
            System.out.println("3. Show Expenses");
            System.out.println("4. Show Summary");
            System.out.println("5. Exit");

            System.out.print("Enter Choice: ");
            choice=sc.nextInt();

            switch(choice)
            {
                case 1:
                addExpense();
                break;

                case 2:
                deleteExpense();
                break;

                case 3:
                showExpenses();
                break;

                case 4:
                showSummary();
                break;

                case 5:
                System.out.println("Thank you!");
                break;

                default:
                System.out.println("Invalid choice");
            }

        }while(choice!=5);

    }

    static void login()
    {
        String u,p;

        while(true)
        {
            System.out.print("Enter Username: ");
            u=sc.next();

            System.out.print("Enter Password: ");
            p=sc.next();

            if(u.equals(username) && p.equals(password))
            {
                System.out.println("Login Successful!");
                break;
            }
            else
            {
                System.out.println("Wrong login details. Try again.");
            }
        }
    }

    static void addExpense()
    {
        System.out.print("Enter Expense Name: ");
        String name=sc.next();

        System.out.print("Enter Amount: ");
        double amount=sc.nextDouble();

        System.out.print("Enter Date: ");
        String date=sc.next();

        Expense e=new Expense(name,amount,date);

        expenses.add(e);

        spent+=amount;

        System.out.println("Expense Added!");
    }

    static void deleteExpense()
    {

        if(expenses.size()==0)
        {
            System.out.println("No expenses to delete");
            return;
        }

        showExpenses();

        System.out.print("Enter expense number to delete: ");
        int n=sc.nextInt();

        if(n>0 && n<=expenses.size())
        {
            spent-=expenses.get(n-1).amount;
            expenses.remove(n-1);
            System.out.println("Expense Deleted");
        }
        else
        {
            System.out.println("Invalid number");
        }
    }

    static void showExpenses()
    {

        if(expenses.size()==0)
        {
            System.out.println("No expenses added");
            return;
        }

        System.out.println("\n--- Expense List ---");

        for(int i=0;i<expenses.size();i++)
        {
            Expense e=expenses.get(i);

            System.out.println((i+1)+". "+e.name+
            " | "+e.amount+
            " | "+e.date);
        }
    }

    static void showSummary()
    {
        double remaining=totalMoney-spent;

        System.out.println("\n---- SUMMARY ----");
        System.out.println("Total Money : "+totalMoney);
        System.out.println("Spent Money : "+spent);
        System.out.println("Remaining   : "+remaining);
    }
}