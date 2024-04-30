package minigit;

public class Main {

    /**
     * Usage: java minigit.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        Repository r = new Repository();

        if (args.length == 0) {
            r.exitWithError("Please enter a command.");
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                r.validateNumArgs("init", args, 1);
                r.init();
            }
            case "add" -> {
                r.validateNumArgs("add", args, 2);
                r.validateInit();
                r.add(args);
            }
            case "commit" -> {
                r.validateLessThan("commit", args, 2);
                r.validateInit();
                r.commit(args);
            }
            case "rm" -> {
                r.validateNumArgs("rm", args, 2);
                r.validateInit();
                r.rm(args);
            }
            case "log" -> {
                r.validateNumArgs("log", args, 1);
                r.validateInit();
                r.log();
            }
            case "global-log" -> {
                r.validateNumArgs("global-log", args, 1);
                r.validateInit();
                r.globalLog();
            }
            case "find" -> {
                r.validateNumArgs("find", args, 2);
                r.validateInit();
                r.find(args);
            }
            case "checkout" -> {
                r.validateLessThan("checkout", args, 4);
                r.validateInit();
                r.checkout(args);
            }
            case "branch" -> {
                r.validateNumArgs("branch", args, 2);
                r.validateInit();
                r.branch(args);
            }
            case "rm-branch" -> {
                r.validateNumArgs("rm-branch", args, 2);
                r.validateInit();
                r.rmBranch(args);
            }
            case "reset" -> {
                r.validateNumArgs("reset", args, 2);
                r.validateInit();
                r.reset(args);
            }
            case "status" -> {
                r.validateNumArgs("status", args, 1);
                r.validateInit();
                r.status();
            }
            default -> r.exitWithError("No command with that name exists.");
        }
    }

}