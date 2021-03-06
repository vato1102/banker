import banker.*;
import banker.errors.*;

import java.util.*;
import java.io.*;

public class Program {
    private int resourceCount;
    private int customerCount;

    private Bank bank;
    private Customer[] customers;

    public static void main(String args[]) {
        try {
            Program prog = new Program(args);
            prog.start();
        } catch (MissingArgErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (ParseErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        } catch (InvalidArgumentErr e) {
            System.out.println("Error: " + e.getMessage());
            Program.printHelp();
            System.exit(0);
        }
    }

    // prints the help message to standard output
    public static void printHelp() {
        String programName = "banker";
        String str = "";
        str += "Usage: " + programName;
        str += " resources customers";
        System.out.println(str);
    }

    public Program(String args[]) throws ParseErr, MissingArgErr, InvalidArgumentErr {
        this.parseArguments(args);
        // this.manualInit();  // this is temporary
        this.bank = this.initBank();
        this.initCustomers(this.bank);
        this.initRandomCustomers();
    }

    public void start() {
        Thread[] pids = new Thread[this.customerCount];

        // spin up all customer threads
        for (int i = 0; i < this.customerCount; i += 1) {
            pids[i] = new Thread(this.customers[i]);
            pids[i].start();
        }



        // Wait for all customer threads to be done
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < pids.length; i += 1 ) {
                if (pids[i].isAlive()) {
                    flag = true;
                    Thread.yield();
                }
            }
        }
    }

    // maybe throw error on problem
    private void parseArguments(String args[]) throws ParseErr, MissingArgErr, InvalidArgumentErr {
        if (args.length < 2) {
            throw new MissingArgErr();
        }
        try {
            this.resourceCount = Integer.valueOf(args[0]);
            int min = Bank.MIN_RESOURCE;
            int max = Bank.MAX_RESOURCE;
            if (this.resourceCount < min ||
                this.resourceCount > max) {
                String low = String.valueOf(min);
                String high = String.valueOf(max);
                throw new InvalidArgumentErr(args[0] + " needs to be between " + low + " and " + high);
            }
        } catch (NumberFormatException e) {
            throw new ParseErr(args[0] + " is not a valid number of resources");
        }

        try {
            this.customerCount = Integer.valueOf(args[1]);
        } catch (NumberFormatException e) {
            throw new ParseErr(args[1] + " is not a valid number of customers");
        }
    }

    private Bank initBank() {
        Bank bank = new Bank.Builder()
            .randomResources(this.resourceCount)
            .randomMaximum(this.customerCount, this.resourceCount)
            .build();
        return bank;
    }

    private void initCustomers(Bank bank) {
        this.customers = new Customer[this.customerCount];
        for (int i = 0; i < this.customerCount; i += 1) {
            this.customers[i] = new Customer(i, bank);
        }
    }

    private void manualInit() {
        int[][] max = new int[this.customerCount][this.resourceCount];
        max[0][0]= 7; max[0][1]= 5; max[0][2]= 3;
        max[1][0]= 3; max[1][1]= 2; max[1][2]= 2;
        max[2][0]= 9; max[2][1]= 0; max[2][2]= 2;
        max[3][0]= 2; max[3][1]= 2; max[3][2]= 2;
        max[4][0]= 4; max[4][1]= 3; max[4][2]= 3;

        int[] resources = new int[3];
        resources[0] = 10; resources[1] = 5; resources[2] = 7;
        this.bank = new Bank.Builder()
            .resources(resources)
            .maximum(max)
            .build();

        this.customers = new Customer[5];
        for (int i = 0; i < 5; i += 1) {
            this.customers[i] = new Customer(i, this.bank);
        }


        int[] request0 = new int[3];
        request0[0] = 0; request0[1] = 1; request0[2] = 0;
        this.customers[0].addRequest(request0);
        this.customers[0].close();

        int[] request1 = new int[3];
        request1[0] = 2; request1[1] = 0; request1[2] = 0;
        this.customers[1].addRequest(request1);
        this.customers[1].close();

        int[] request2 = new int[3];
        request2[0] = 3; request2[1] = 0; request2[2] = 2;
        this.customers[2].addRequest(request2);
        this.customers[2].close();

        int[] request3 = new int[3];
        request3[0] = 2; request3[1] = 1; request3[2] = 1;
        this.customers[3].addRequest(request3);
        this.customers[3].close();

        int[] request4 = new int[3];
        request3[0] = 0; request3[1] = 0; request3[2] = 1;
        this.customers[4].addRequest(request3);
        this.customers[4].close();
    }

    private void initRandomCustomers() {
        // load requests
        for (int i = 0; i < this.customerCount; i += 1) {
            this.customers[i].newRandomRequests(3);
            this.customers[i].close();
        }
    }
}
