package seedu.finbro.logic.command;

import seedu.finbro.model.TransactionManager;
import seedu.finbro.storage.Storage;
import seedu.finbro.ui.Ui;

//TODO: update HelpCommand to use the new friendly CLI

/**
 * Represents a help command.
 */
public class HelpCommand implements Command {
    /**
     * Executes the command to display help information.
     *
     * @param transactionManager The transaction manager
     * @param ui                 The UI to interact with the user
     * @param storage            The storage to save data
     * @return The help message
     */
    @Override
    public String execute(TransactionManager transactionManager, Ui ui, Storage storage) {
        return "Available command words:\n\n" +
                "expense  | AMOUNT , d/DESCRIPTION , [c/CATEGORY] , [t/TAG...] => Add an expense transaction\n" +
                "income   | AMOUNT , d/DESCRIPTION , [t/TAG...] => Add an income transaction\n" +
                "list     | [d/DATE] , [n/NUMBER] => List transactions\n" +
                "delete   | INDEX => Delete a transaction\n" +
                "edit     | KEYWORD , [a/AMOUNT] , [d/DESCRIPTION] , [date/DATE] , [c/CATEGORY] , [t/TAG] " +
                "=> Edit a transaction\n" +
                "filter   | d/DATE , [to/DATE] =>  Filter transactions by date\n" +
                "summary  | [m/MONTH] [y/YEAR] - View financial summary\n" +
                "export   | [f/FORMAT] - Export data (csv or txt)\n" +
                "search   | KEYWORD => Search transactions\n" +
                "balance  | View current balance\n" +
                "exit     | Exit the program\n" +
                "clear    | Clear all data\n" +
                "help     | Show this help message";
    }

    /**
     * Returns false since this is not an exit command.
     *
     * @return false
     */
    @Override
    public boolean isExit() {
        return false;
    }
}
