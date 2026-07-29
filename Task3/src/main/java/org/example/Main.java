package org.example;

import org.example.constants.AccountStatus;
import org.example.models.Account;
import org.example.models.Transaction;
import org.example.services.AccountServices;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main{
    public static AccountServices accountServices = new AccountServices();
    public static Scanner sc = new Scanner(System.in);
    public static void main(String[] args){


        boolean chance = true;
        while(chance){
            displayMenu();
            int choice = sc.nextInt();
            switch (choice){
                case 1 : createAccount();
                        break;
                case 2 : depositMoney();
                        break;
                case 3 : withdrawalMoney();
                        break;
                case 4 : blockAccount();
                        break;
                case 5 : activateAccount();
                        break;
                case 6 : checkBalance();
                       break;
                case 7 : getTransactionHistory();
                       break;
                case 8 :
                    chance = false;
                    System.out.println("Transaction Ended ! Thank You For Using the Bank Account");
                    break;
                default:
                    System.out.println("Invalid Choice , try Again ");
            }

        }

    }

    public static void displayMenu(){
        System.out.println("Please Select the Option");
        System.out.println("1. Create Account");
        System.out.println("2. Deposit Amount");
        System.out.println("3. Withdrawal Amount");
        System.out.println("4. Block Account");
        System.out.println("5. Activate Account");
        System.out.println("6. Check Current Balance");
        System.out.println("7. View Transaction History");
        System.out.println("8. Exit");

    }

    public static void createAccount(){
        System.out.println("Enter Account Number , Account Holder Name , Initial Balance");
        long accNumber = sc.nextLong();
        sc.nextLine();
        String accName = sc.nextLine();
        BigDecimal initBalance = sc.nextBigDecimal();
        sc.nextLine();

        try{
            if (initBalance.compareTo(BigDecimal.valueOf(0.0)) == 0){
                accountServices.createAccount(accNumber,accName);
            }else{
                accountServices.createAccount(accNumber,accName,initBalance);
            }
            System.out.println("Account created Successfully");
        }catch (Exception e){
            System.out.println("Error : " + e.getMessage());
        }

    }

    public static void depositMoney(){
        System.out.println("Enter the Account Number and the Deposit Amount");
        long accNumber = sc.nextLong();
        sc.nextLine();
        BigDecimal depositAmt = sc.nextBigDecimal();
        try{
            BigDecimal balance = accountServices.deposit(accNumber,depositAmt);
            System.out.println("Amount Deposit Successfully. The Remaining Balance is " + balance );
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }

    }
    public static void withdrawalMoney(){
        System.out.println("Enter the Account Number and the Withdrawal Amount");
        long accNumber = sc.nextLong();
        sc.nextLine();
        BigDecimal withdrawalAmt = sc.nextBigDecimal();
        try{
            BigDecimal balance = accountServices.withdraw(accNumber,withdrawalAmt);
            System.out.println("Amount Withdrawn Successfully . The Remaining Balance is "+ balance);
        }catch (Exception e){
            System.out.println("Error : " + e.getMessage() );
        }
    }
    public static void blockAccount(){
        System.out.println("Enter the Account Number");
        long accNumber = sc.nextLong();
        try{
            Account acc = accountServices.getAccountDetails(accNumber);
            if(acc.getAccountStatus() == AccountStatus.BLOCKED) System.out.println("Account is already Blocked");
            else {
                accountServices.blockAccount(accNumber);
                System.out.println("Account is Successfully Blocked");
            }
        }catch (Exception e){
            System.out.println("Error : " + e.getMessage());
        }


    }
    public static void activateAccount(){
        System.out.println("Enter the Account Number");
        long accNumber = sc.nextLong();
        try{
            Account acc = accountServices.getAccountDetails(accNumber);
            if(acc.getAccountStatus() == AccountStatus.ACTIVE) System.out.println("Account is already Active");
            else {
                accountServices.activateAccount(accNumber);
                System.out.println("Account is Successfully Activated");
            }
        }catch (Exception e){
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void checkBalance(){
        System.out.println("Enter the Account Number");
        long accNumber = sc.nextLong();
        try{
            BigDecimal balance = accountServices.getBalance(accNumber);
            System.out.println("Your Account Balance is : " + balance);

        }catch (Exception e){
            System.out.println("Error : " + e.getMessage());
        }
    }

    public static void getTransactionHistory(){
        System.out.println("Enter the Account Number");
        long accNumber = sc.nextLong();
        try{
            if (accountServices.accountExists(accNumber)){
                Account acc = accountServices.getAccountDetails(accNumber);
                System.out.println("Account Number : "+ acc.getAccountNumber());
                System.out.println("Account Name : "+ acc.getAccountHolderName());
                System.out.println("Current Balance : "+acc.getCurrentBalance());
                System.out.println("Account Status : "+ acc.getAccountStatus());
                System.out.println();
                System.out.println("Transactions : ");
                List<Transaction> transHistory = acc.getTransactionHistory();
                if (transHistory.isEmpty()){
                    System.out.println("No Transaction History found for the above Account Number");
                }else{
                    for (Transaction transaction : transHistory) {
                        System.out.println("Transaction ID : "+transaction.TransactionID());
                        System.out.println("Transaction Type : "+transaction.type());
                        System.out.println("Transaction Amount : "+transaction.amount());
                        System.out.println("Amount Before Transaction : "+transaction.balanceBefore());
                        System.out.println("Amount After Transaction : "+transaction.balanceAfter());
                        System.out.println("TimeStamp of Transaction : "+transaction.timestamp());
                        System.out.println();
                    }

                }

            }

        }catch (Exception e){
            System.out.println("Error : "+e.getMessage());
        }

    }



}