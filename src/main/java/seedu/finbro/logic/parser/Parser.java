package seedu.finbro.logic.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import seedu.finbro.logic.command.BalanceCommand;
import seedu.finbro.logic.command.ClearCommand;
import seedu.finbro.logic.command.Command;
import seedu.finbro.logic.command.DeleteCommand;
import seedu.finbro.logic.command.EditCommand;
import seedu.finbro.logic.command.ExitCommand;
import seedu.finbro.logic.command.ExpenseCommand;
import seedu.finbro.logic.command.ExportCommand;
import seedu.finbro.logic.command.FilterCommand;
import seedu.finbro.logic.command.HelpCommand;
import seedu.finbro.logic.command.IncomeCommand;
import seedu.finbro.logic.command.InvalidCommand;
import seedu.finbro.logic.command.ListCommand;
import seedu.finbro.logic.command.SearchCommand;
import seedu.finbro.logic.command.SummaryCommand;
import seedu.finbro.logic.command.UnknownCommand;
import seedu.finbro.logic.exceptions.IndexExceedLimitException;
import seedu.finbro.logic.exceptions.NegativeNumberException;
import seedu.finbro.model.Expense;
import seedu.finbro.model.TransactionManager;
import seedu.finbro.storage.Storage;
import seedu.finbro.ui.Ui;

/**
 * Parses user input and creates the corresponding command.
 */
public class Parser {
    private static final Logger logger = Logger.getLogger(Parser.class.getName());
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Track if a clear confirmation is pending
    private boolean clearCommandPending = false;

    /**
     * Parses user input into a Command.
     *
     * @param userInput Full user input string
     * @return The command based on the user input
     */
    public Command parseCommand(String userInput) {
        assert userInput != null : "User input cannot be null";

        logger.fine("Parsing user input: " + userInput);
        userInput = userInput.trim();
        if (userInput.isEmpty()) {
            logger.fine("Empty input, returning HelpCommand");
            clearCommandPending = false;
            return new HelpCommand();
        }

        String[] parts = userInput.split("\\s+", 2);
        String commandWord = parts[0].toLowerCase();
        String arguments = parts.length > 1 ? parts[1] : "";

        // Handle y/n responses if a clear confirmation is pending
        if (clearCommandPending) {
            if (commandWord.equals("y") || commandWord.equals("yes")) {
                clearCommandPending = false;
                logger.fine("Clear command confirmed with 'y'");
                return new ClearCommand(true);
            } else if (commandWord.equals("n") || commandWord.equals("no")) {
                clearCommandPending = false;
                logger.fine("Clear command cancelled with 'n'");
                return new Command() {
                    @Override
                    public String execute(TransactionManager transactionManager, Ui ui, Storage storage) {
                        return "Clear operation cancelled.";
                    }

                    @Override
                    public boolean isExit() {
                        return false;
                    }
                };
            }
            // If not y/n, reset the pending flag
            clearCommandPending = false;
        }

        assert commandWord != null && !commandWord.isEmpty() : "Command word cannot be null or empty";
        logger.fine("Command word: " + commandWord + ", Arguments: " + arguments);

        Command parsedCommand;
        switch (commandWord) {
        case "search":
            parsedCommand = parseSearchCommand(arguments);
            break;
        case "income":
            parsedCommand = parseIncomeCommand(arguments);
            break;
        case "expense":
            parsedCommand = parseExpenseCommand(arguments);
            break;
        case "list":
            parsedCommand = parseListCommand(arguments);
            break;
        case "delete":
            parsedCommand = parseDeleteCommand(arguments);
            break;
        case "filter":
            parsedCommand = parseFilterCommand(arguments);
            break;
        case "balance":
        case "view": // Support for "view" as an alias for "balance"
            parsedCommand = new BalanceCommand();
            break;
        case "summary":
            parsedCommand = parseSummaryCommand(arguments);
            break;
        case "export":
            parsedCommand = parseExportCommand(arguments);
            break;
        case "clear":
            parsedCommand = parseClearCommand(arguments);
            // Set the flag if this is the initial clear command (without confirm)
            if (arguments.trim().isEmpty()) {
                clearCommandPending = true;
            }
            break;
        case "exit":
            parsedCommand = new ExitCommand();
            break;
        case "help":
            parsedCommand = new HelpCommand();
            break;
        case "edit":
            parsedCommand = parseEditCommand(arguments);
            break;
        default:
            logger.warning("Unknown command: " + commandWord);
            parsedCommand = new UnknownCommand(commandWord);
            break;
        }

        assert parsedCommand != null : "Parsed command cannot be null";
        logger.fine("Parsed command: " + parsedCommand.getClass().getSimpleName());
        return parsedCommand;
    }

    /*
    New method to parse command word only to implement friendly CLI
    Edit switch cases after editing each parse method
     */
    public Command parseCommandWord(String commandWord, Ui ui) {
        assert commandWord != null && !commandWord.isEmpty() : "Command word cannot be null or empty";
        logger.fine("Command word: " + commandWord);

        commandWord = commandWord.trim();
        if (commandWord.isEmpty()) {
            logger.fine("Empty input, returning HelpCommand");
            clearCommandPending = false;
            return new HelpCommand();
        }

        // Handle y/n responses if a clear confirmation is pending
        if (clearCommandPending) {
            if (commandWord.equals("y") || commandWord.equals("yes")) {
                clearCommandPending = false;
                logger.fine("Clear command confirmed with 'y'");
                return new ClearCommand(true);
            } else if (commandWord.equals("n") || commandWord.equals("no")) {
                clearCommandPending = false;
                logger.fine("Clear command cancelled with 'n'");
                return new Command() {
                    @Override
                    public String execute(TransactionManager transactionManager, Ui ui, Storage storage) {
                        return "Clear operation cancelled.";
                    }

                    @Override
                    public boolean isExit() {
                        return false;
                    }
                };
            }
            // If not y/n, reset the pending flag
            clearCommandPending = false;
        }

        Command parsedCommand;
        switch (commandWord) {
        case "search":
            parsedCommand = parseSearchCommand(ui);
            break;
        case "income":
            parsedCommand = parseIncomeCommand(ui);
            break;
        case "expense":
            parsedCommand = parseExpenseCommand(ui);
            break;
        case "list":
            parsedCommand = parseListCommand(ui);
            break;
        case "delete":
            parsedCommand = parseDeleteCommand(ui);
            break;
        case "filter":
            parsedCommand = parseFilterCommand(ui);
            break;
        case "balance":
        case "view": // Support for "view" as an alias for "balance"
            parsedCommand = new BalanceCommand();
            break;
        case "summary":
            parsedCommand = parseSummaryCommand(ui);
            break;
        case "export":
        case "clear":
        case "exit":
            parsedCommand = new ExitCommand();
            break;
        case "help":
            parsedCommand = new HelpCommand();
            break;

        case "edit":
        default:
            logger.warning("Unknown command: " + commandWord);
            parsedCommand = new UnknownCommand(commandWord);
            break;
        }

        assert parsedCommand != null : "Parsed command cannot be null";
        logger.fine("Parsed command: " + parsedCommand.getClass().getSimpleName());
        return parsedCommand;
    }

    /**
     * Parses arguments into an EditCommand.
     *
     * @param args Command arguments
     * @return The EditCommand
     */
    private Command parseEditCommand(String args) {
        logger.fine("Parsing edit command with arguments: " + args);
        try {
            String[] parts = args.trim().split("\\s+", 2);

            if (parts.length < 2) {
                logger.warning("Missing parameters for edit command");
                return new InvalidCommand("Please provide a transaction keyword and parameters to edit.");
            }

            String keyword = parts[0];
            Map<String, String> parameters = parseParameters(parts[1]);

            if (parameters.isEmpty()) {
                logger.warning("No edit parameters provided");
                return new InvalidCommand("Please specify at least one parameter to edit.");
            }

            logger.fine("Creating EditCommand with keyword=" + keyword + ", parameters=" + parameters);
            return new EditCommand(keyword, parameters);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing edit command", e);
            return new InvalidCommand("Invalid edit command: " + e.getMessage());
        }
    }

    /**
     * Parses arguments into a ListCommand.
     *
     * @param args Command arguments
     * @return The ListCommand
     */
    private Command parseListCommand(String args) {
        try {
            Map<String, String> parameters = parseParameters(args);

            Integer limit = null;
            if (parameters.containsKey("n")) {
                limit = Integer.parseInt(parameters.get("n"));
                if (limit <= 0) {
                    return new InvalidCommand("Number of transactions must be positive.");
                }
            }

            LocalDate date = null;
            if (parameters.containsKey("d")) {
                try {
                    date = LocalDate.parse(parameters.get("d"), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    return new InvalidCommand("Invalid date format. Please use YYYY-MM-DD.");
                }
            }

            return new ListCommand(limit, date);
        } catch (NumberFormatException e) {
            return new InvalidCommand("Invalid number format.");
        } catch (Exception e) {
            return new InvalidCommand("Invalid list command: " + e.getMessage());
        }
    }

    /**
     * Parses dates read from the UI into ListCommand.
     *
     * @param ui The UI to interact with the user
     * @return The ListCommand
     */
    private Command parseListCommand(Ui ui) {
        logger.fine("Parsing list command");
        try {
            String startDateInput = ui.readStartDate(); //only uses start date
            logger.fine("List start date: " + startDateInput);

            LocalDate date = null;
            if (startDateInput != null && !startDateInput.isEmpty()) {
                date = LocalDate.parse(startDateInput.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            Integer limit = ui.readLimit();
            if (limit != null && limit <= 0) {
                logger.warning("Invalid limit: " + limit);
                return new InvalidCommand("Number of transactions must be a positive integer.");
            }

            logger.fine("Creating ListCommand with limit=" + limit + ", date=" + date);
            return new ListCommand(limit, date);

        } catch (DateTimeParseException e) {
            logger.log(Level.WARNING, "Date format error in list command", e);
            return new InvalidCommand("Date must be specified in the format YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid number format for list limit", e);
            return new InvalidCommand("Limit must be a valid number.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing list command", e);
            return new InvalidCommand("Invalid list command: " + e.getMessage());
        }
    }

    /**
     * Parses arguments into a DeleteCommand.
     *
     * @param args Command arguments
     * @return The DeleteCommand
     */
    private Command parseDeleteCommand(String args) {
        try {
            int index = Integer.parseInt(args.trim());
            if (index <= 0) {
                return new InvalidCommand("Index must be a positive integer.");
            }
            return new DeleteCommand(index);
        } catch (NumberFormatException e) {
            return new InvalidCommand("Invalid index. Please provide a valid number.");
        } catch (Exception e) {
            return new InvalidCommand("Invalid delete command: " + e.getMessage());
        }
    }

    /**
     * Parses index read from parseInteger into a DeleteCommand.
     *
     * @param ui The UI to interact with the user
     * @return The DeleteCommand
     */
    private Command parseDeleteCommand(Ui ui) {
        logger.fine("Parsing delete command");
        try {
            String message = "Enter the index of the transaction to delete. \n> ";
            int index = parseInteger(ui, message);
            logger.fine("User input index: " + index);

            if (index == -696969) {
                logger.warning("non-integer was entered, error should be caught");
                return new InvalidCommand("Non-integer was passed as index.");
            }

            if (index <= 0) {
                logger.warning("Invalid index: " + index + " (must be positive)");
                return new InvalidCommand("Index must be a positive integer.");
                //TODO make recursively ask for new index
            }

            logger.fine("Creating DeleteCommand with index =" + index);
            return new DeleteCommand(index);

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid number format for delete index", e);
            return new InvalidCommand("Invalid index. Please provide a valid number.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing delete command", e);
            return new InvalidCommand("Invalid delete command: " + e.getMessage());
        }
    }


    /**
     * Parses arguments into a FilterCommand.
     *
     * @param args Command arguments
     * @return The FilterCommand
     */
    private Command parseFilterCommand(String args) {
        logger.fine("Parsing filter command with arguments: " + args);
        try {
            Map<String, String> parameters = parseParameters(args);
            logger.fine("Parsed parameters: " + parameters);

            if (!parameters.containsKey("d")) {
                logger.warning("Missing date parameter for filter command");
                return new InvalidCommand("Start date must be specified for filter command.");
            }

            LocalDate startDate = LocalDate.parse(parameters.get("d"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate endDate;

            if (!parameters.containsKey("to")) {
                logger.fine("No end date specified, using current date");
                endDate = LocalDate.now();
            } else {
                endDate = LocalDate.parse(parameters.get("to"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }

            if (startDate.isAfter(endDate)) {
                logger.warning("Start date is after end date: " + startDate + " > " + endDate);
                return new InvalidCommand("Start date cannot be after end date.");
            }

            logger.fine("Creating FilterCommand with startDate=" + startDate +
                    ", endDate=" + endDate);
            return new FilterCommand(startDate, endDate);
        } catch (DateTimeParseException e) {
            logger.log(Level.WARNING, "Date format error in filter command", e);
            return new InvalidCommand("Date must be specified in the format YYYY-MM-DD.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing filter command", e);
            return new InvalidCommand("Invalid filter command: " + e.getMessage());
        }
    }

    /**
     * Parses dates read from the UI into a FilterCommand.
     *
     * @param ui The UI to interact with the user
     * @return The FilterCommand
     */
    //method for parsing filter command for friendly CLI
    private Command parseFilterCommand(Ui ui) {
        logger.fine("Parsing filter command");
        try {
            String[] filterDates = ui.readDates();
            logger.fine("Filter dates: " + filterDates[0] + " to " + filterDates[1]);

            if (filterDates[0] == null || filterDates[0].isEmpty()) {
                logger.warning("Missing start date parameter for filter command");
                return new InvalidCommand("Start date must be specified for filter command.");
            }

            LocalDate startDate = LocalDate.parse(
                filterDates[0].trim(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            );

            LocalDate endDate;
            if (filterDates[1] == null || filterDates[1].isEmpty()) {
                logger.fine("No end date specified, using current date");
                endDate = LocalDate.now();
            } else {
                endDate = LocalDate.parse(
                    filterDates[1].trim(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                );
            }

            if (startDate.isAfter(endDate)) {
                logger.warning("Start date is after end date: " + startDate + " > " + endDate);
                return new InvalidCommand("Start date cannot be after end date.");
            }

            logger.fine("Creating FilterCommand with startDate=" + startDate +
                ", endDate=" + endDate);
            return new FilterCommand(startDate, endDate);
        } catch (DateTimeParseException e) {
            logger.log(Level.WARNING, "Date format error in filter command", e);
            return new InvalidCommand("Date must be specified in the format YYYY-MM-DD.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing filter command", e);
            return new InvalidCommand("Invalid filter command: " + e.getMessage());
        }
    }


    /**
     * Parses arguments into a SummaryCommand with specified month and year
     * @param args Command arguments
     * @return The SummaryCommand
     */
    private Command parseSummaryCommand(String args) {
        Map<String, String> parameters = parseParameters(args);

        try {
            int month;
            int year;
            if (!parameters.containsKey("m")) {
                month = LocalDate.now().getMonthValue();
            } else {
                month = Integer.parseInt(parameters.get("m"));
                if (month < 1 || month > 12) {
                    return new InvalidCommand("Month input must be between 1 and 12.");
                }
            }
            if (!parameters.containsKey("y")) {
                year = LocalDate.now().getYear();
            } else {
                year = Integer.parseInt(parameters.get("y"));
                if (year % 1000 == 0 || year > LocalDate.now().getYear()) {
                    return new InvalidCommand("Year input must be 4-digit.");
                }
            }
            return new SummaryCommand(month, year);
        } catch (Exception e) {
            return new InvalidCommand("Invalid summary command: " + e.getMessage());
        }
    }

    /**
     * Parses month and year read from ui into a SummaryCommand.
     *
     * @param ui The UI to interact with the user
     * @return The SummaryCommand
     */
    //method for parsing summary command for friendly CLI
    private Command parseSummaryCommand(Ui ui) {
        logger.fine("Parsing summary command");
        try {
            Integer[] monthYear = ui.readMonthYear();
            Integer month = monthYear[0];
            Integer year = monthYear[1];

            if (month == null) {
                logger.info("No month input, using current month");
                month = LocalDate.now().getMonthValue();
            } else if (month < 1 || month > 12) {
                logger.warning("Invalid month input: " + month);
                return new InvalidCommand("Month input must be between 1 and 12.");
            }
            if (year == null) {
                logger.info("No year input, using current year");
                year = LocalDate.now().getYear();
            } else if (year % 1000 == 0 || year > LocalDate.now().getYear()) {
                logger.warning("Invalid year input: " + year);
                return new InvalidCommand("Year input must be 4-digit and cannot be after current year.");
            }
            return new SummaryCommand(month, year);
        } catch (Exception e) {
            return new InvalidCommand("Invalid summary command: " + e.getMessage());
        }
    }

    /**
     * Parses arguments into an ExportCommand.
     *
     * @param args Command arguments
     * @return The ExportCommand
     */
    private Command parseExportCommand(String args) {
        logger.fine("Parsing export command with arguments: " + args);
        try {
            Map<String, String> parameters = parseParameters(args);
            logger.fine("Parsed parameters: " + parameters);

            String format = "csv"; // Default format
            if (parameters.containsKey("f")) {
                format = parameters.get("f").toLowerCase();
                if (!format.equals("csv") && !format.equals("txt")) {
                    logger.warning("Invalid export format: " + format);
                    return new InvalidCommand("Export format must be either 'csv' or 'txt'.");
                }
            }

            logger.fine("Creating ExportCommand with format=" + format);
            return new ExportCommand(format);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing export command", e);
            return new InvalidCommand("Invalid export command: " + e.getMessage());
        }
    }

    /**
     * Parses arguments into a ClearCommand.
     *
     * @param args Command arguments
     * @return The ClearCommand
     */
    private Command parseClearCommand(String args) {
        logger.fine("Parsing clear command with arguments: " + args);
        boolean isConfirmed = args.trim().equalsIgnoreCase("confirm");
        logger.fine("Clear confirmation status: " + isConfirmed);
        return new ClearCommand(isConfirmed);
    }

    /**
     * Parses a string into parameters.
     *
     * @param paramString The string containing parameters
     * @return A map of parameter prefixes to values
     */
    private Map<String, String> parseParameters(String paramString) {
        assert paramString != null : "Parameter string cannot be null";

        Map<String, String> parameters = new HashMap<>();
        String[] tokens = paramString.trim().split("\\s+");

        // Handle the first parameter (usually amount)
        if (tokens.length > 0 && !tokens[0].contains("/")) {
            parameters.put("", tokens[0]);
        }

        // Process the rest of the parameters
        String currentPrefix = null;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            assert token != null : "Token cannot be null";

            // Skip the first token if it was already processed as the main parameter
            if (i == 0 && parameters.containsKey("")) {
                continue;
            }

            // Check if this token starts a new parameter
            if (token.matches("[a-zA-Z]+/.*")) {
                // Save the previous parameter if it exists
                if (currentPrefix != null) {
                    parameters.put(currentPrefix, currentValue.toString().trim());
                    currentValue = new StringBuilder();
                }

                // Extract the new prefix and start building its value
                int slashIndex = token.indexOf('/');
                assert slashIndex >= 0 : "Expected slash in parameter token";
                currentPrefix = token.substring(0, slashIndex);
                currentValue.append(token.substring(slashIndex + 1));
            } else if (currentPrefix != null) {
                // Continue building the current parameter value
                currentValue.append(" ").append(token);
            }
        }

        // Save the last parameter if it exists
        if (currentPrefix != null) {
            parameters.put(currentPrefix, currentValue.toString().trim());
        }

        assert parameters != null : "Parsed parameters map cannot be null";
        return parameters;
    }

    /**
     * Parses a double from the user using the UI.
     * Returns the parsed amount or calls the function again if input is invalid.
     *
     * @param ui The UI to read input from.
     * @param message The message to print to the terminal.
     * @return The parsed amount as a double.
     */
    private double parseAmount(Ui ui, String message) {
        try {
            double amount = ui.readDouble(message);
            if (amount <= 0) {
                logger.warning("Invalid amount input: " + amount);
                return parseAmount(ui, message);
            }
            return amount;
        } catch (NumberFormatException e) {
            logger.warning("Non-numeric input received for amount.");
            return parseAmount(ui, message);
        } catch (Exception e) {
            logger.warning("Unexpected error while parsing amount: " + e.getMessage());
            return parseAmount(ui, message);
        }
    }

    /**
     * LEGACY CODE
     * Parses a string into a valid amount.
     *
     * @param amountStr The string to parse
     * @return The amount as a double
     * @throws NumberFormatException if the string is not a valid amount
     */
    private double parseAmount(String amountStr) throws NumberFormatException {
        assert amountStr != null : "Amount string cannot be null";
        assert !amountStr.trim().isEmpty() : "Amount string cannot be empty";

        if (!AMOUNT_PATTERN.matcher(amountStr).matches()) {
            throw new NumberFormatException("Invalid amount format.");
        }

        double amount = Double.parseDouble(amountStr);
        assert amount > 0 : "Amount must be greater than zero";
        return amount;
    }

    /**
     * LEGACY CODE
     * Extracts tags from parameters.
     *
     * @param parameters The parameters map
     * @return A list of tags
     */
    private List<String> parseTags(Map<String, String> parameters) {
        List<String> tags = new ArrayList<>();

        // Look for parameters with prefix 't'
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().equals("t")) {
                tags.add(entry.getValue());
            }
        }

        return tags;
    }

    /**
     * Prompts the user to input up to 3 tags and returns them as a list.
     * Tags are split by commas or spaces and trimmed. Empty input returns an empty list.
     *
     * @param ui The UI to interact with the user.
     * @return A list of up to 3 trimmed tags.
     */
    private List<String> parseTags(Ui ui) {
        List<String> tags = new ArrayList<>();
        String input = ui.readTags("Enter up to 3 tags (separated by space or comma), or press Enter to skip:\n> ");

        if (input.isEmpty()) {
            return tags;
        }

        String[] rawTags = input.split("[,\\s]+");

        for (String tag : rawTags) {
            String trimmed = tag.trim();
            if (!trimmed.isEmpty()) {
                tags.add(trimmed);
            }
            if (tags.size() == 3) {
                break;
            }
        }

        return tags;
    }

    /**
     * LEGACY CODE
     * Parses arguments into an ExpenseCommand.
     *
     * @param args Command arguments
     * @return The ExpenseCommand
     */
    private Command parseExpenseCommand(String args) {
        logger.fine("Parsing expense command with arguments: " + args);
        try {
            Map<String, String> parameters = parseParameters(args);
            logger.fine("Parsed parameters: " + parameters);

            if (!parameters.containsKey("")) {
                logger.warning("Missing amount parameter for expense command");
                return new InvalidCommand("Amount is required for expense command.");
            }

            if (!parameters.containsKey("d")) {
                logger.warning("Missing description parameter for expense command");
                return new InvalidCommand("Description is required for expense command.");
            }

            double amount = parseAmount(parameters.get(""));
            String description = parameters.get("d");

            Expense.Category category = Expense.Category.OTHERS;
            if (parameters.containsKey("c")) {
                category = Expense.Category.fromString(parameters.get("c"));
            }

            List<String> tags = parseTags(parameters);

            logger.fine("Creating ExpenseCommand with amount=" + amount +
                    ", description=" + description + ", category=" + category +
                    ", tags=" + tags);
            return new ExpenseCommand(amount, description, category, tags);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid amount format in expense command", e);
            return new InvalidCommand(
                    "Invalid amount format. Please provide a valid number with up to 2 decimal places."
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing expense command", e);
            return new InvalidCommand("Invalid expense command: " + e.getMessage());
        }
    }

    //TODO consider whether catching exception is needed in parse<command>Command since parse methods already handle

    /**
     * ONE COMMAND ONE ACTION OVERRIDE
     * Parses arguments into an ExpenseCommand.
     *
     * @return The ExpenseCommand
     */
    private Command parseExpenseCommand(Ui ui) {
        logger.fine("Parsing expense command with ONE COMMAND ONE ACTION ");
        try {
            double amount = parseAmount(ui, "Enter amount:\n> ");

            String description = ui.readString("Enter description:\n> ");

            Expense.Category category = parseCategory(ui);

            List<String> tags = parseTags(ui);

            return new ExpenseCommand(amount, description, category, tags);

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid amount format in expense command", e);
            return new InvalidCommand(
                    "Invalid amount format. Please provide a valid number with up to 2 decimal places."
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing expense command", e);
            return new InvalidCommand("Invalid expense command: " + e.getMessage());
        }
    }

    /**
     * LEGACY CODE
     * Parses arguments into an IncomeCommand.
     *
     * @param args Command arguments
     * @return The IncomeCommand
     */
    private Command parseIncomeCommand(String args) {
        logger.fine("Parsing income command with arguments: " + args);
        try {
            Map<String, String> parameters = parseParameters(args);
            logger.fine("Parsed parameters: " + parameters);

            if (!parameters.containsKey("")) {
                logger.warning("Missing amount parameter for income command");
                return new InvalidCommand("Amount is required for income command.");
            }

            if (!parameters.containsKey("d")) {
                logger.warning("Missing description parameter for income command");
                return new InvalidCommand("Description is required for income command.");
            }

            double amount = parseAmount(parameters.get(""));
            String description = parameters.get("d");
            List<String> tags = parseTags(parameters);

            logger.fine("Creating IncomeCommand with amount=" + amount +
                    ", description=" + description + ", tags=" + tags);
            return new IncomeCommand(amount, description, tags);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid amount format in income command", e);
            return new InvalidCommand(
                    "Invalid amount format. Please provide a valid number with up to 2 decimal places."
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing income command", e);
            return new InvalidCommand("Invalid income command: " + e.getMessage());
        }
    }

    /**
     * ONE COMMAND ONE ACTION OVERRIDE
     * Parses arguments into an IncomeCommand.
     *
     * @return The IncomeCommand
     */
    private Command parseIncomeCommand(Ui ui) {
        logger.fine("Parsing income command with friendly CLI ");
        try {
            double amount = parseAmount(ui,"Enter amount:\n");

            String description = ui.readString("Enter description:\n");

            List<String> tags = parseTags(ui);

            return new IncomeCommand(amount, description, tags);

        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid amount format in expense command", e);
            return new InvalidCommand(
                    "Invalid amount format. Please provide a valid number with up to 2 decimal places."
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing expense command", e);
            return new InvalidCommand("Invalid expense command: " + e.getMessage());
        }
    }

    /**
     * LEGACY CODE
     * Parses arguments into a SearchCommand.
     *
     * @param args Command arguments
     * @return The SearchCommand
     */
    private Command parseSearchCommand(String args) {
        logger.fine("Parsing search command with arguments: " + args);
        try {
            if (args.trim().isEmpty()) {
                return new InvalidCommand("Search command requires at least one keyword.");
            }

            Map<String, String> parameters = parseParameters(args);
            logger.fine("Parsed parameters: " + parameters);

            if (!parameters.containsKey("")) {
                logger.warning("Missing keyword for search command");
                return new InvalidCommand("keyword is required for search command.");
            }

            logger.fine("Searching transactions with keyword=" + args);
            return new SearchCommand(args);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing search command", e);
            return new InvalidCommand("Invalid search command: " + e.getMessage());
        }
    }

    /**
     * ONE COMMAND ONE ACTION OVERRIDE
     *  Parses arguments into a SearchCommand.
     *
     * @return The SearchCommand
     */
    private Command parseSearchCommand(Ui ui) {
        logger.fine("Parsing search command with friendly CLI: ");
        try {
            String keyword = ui.readString("Enter keyword or string to search:\n>");

            logger.fine("Searching transactions with keyword=" + keyword);

            return new SearchCommand(keyword);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing search command", e);
            return new InvalidCommand("Invalid search command: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to select a category by index (0–5) and returns the corresponding category.
     *
     * @param ui The UI to read user input.
     * @return The selected category.
     * @throws NegativeNumberException If a negative number is entered.
     * @throws IndexExceedLimitException If the index exceeds the valid range (0–5).
     */
    private Expense.Category parseCategory(Ui ui) throws NegativeNumberException, IndexExceedLimitException {
        try{

            String message = "Please select a category by entering its corresponding index\n" +
                    "0 - OTHERS\n" +
                    "1 - FOOD\n" +
                    "2 - TRANSPORT\n" +
                    "3 - SHOPPING\n" +
                    "4 - BILLS\n" +
                    "5 - ENTERTAINMENT\n" +
                    "> ";

            int catIndex = parseInteger(ui, message);
            if (catIndex < 0) {
                throw new NegativeNumberException();
            }
            if (catIndex > 5) {
                throw new IndexExceedLimitException();
            }
            assert catIndex >= 0;
            switch (catIndex) {
            case 0: return Expense.Category.OTHERS;
            case 1: return Expense.Category.FOOD;
            case 2: return Expense.Category.TRANSPORT;
            case 3: return Expense.Category.SHOPPING;
            case 4: return Expense.Category.BILLS;
            case 5: return Expense.Category.ENTERTAINMENT;
            default: {
                assert catIndex >5 || catIndex < 0;
                System.out.println("INVALID INDEX. PICK AN INDEX FROM 0-5");
                return parseCategory(ui);
            }
            }
        } catch (NegativeNumberException e) {
            logger.log(Level.WARNING, "Category input is negative. Invalid selection", e);
            NegativeNumberException.handle();
        } catch (IndexExceedLimitException e) {
            logger.log(Level.WARNING, "Category input exceeds 5. Invalid selection.", e);
            IndexExceedLimitException.handle();
        }
        return parseCategory(ui);
    }

    /**
     * Parses an integer from the user using the UI.
     * Returns the parsed integer or calls the function again if input is invalid.
     *
     * @param ui The UI to read input from.
     * @param message The message to print to the terminal.
     * @return The parsed index as an integer.
     */
    private int parseInteger(Ui ui, String message) {
        try {
            int index = ui.readInteger(message);
            if (index < 0) {
                logger.warning("Invalid index input: " + index);
                return parseInteger(ui, message);
            }
            return index;
        } catch (NumberFormatException e) {
            logger.warning("Non-integer input received for index.");
            return parseInteger(ui, message);
        } catch (Exception e) {
            logger.warning("Unexpected error while parsing index: " + e.getMessage());
            return parseInteger(ui, message);
        }
    }
}
