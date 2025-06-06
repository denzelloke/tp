package seedu.finbro.logic.command;

import seedu.finbro.model.TransactionManager;
import seedu.finbro.storage.Storage;
import seedu.finbro.ui.Ui;

/**
 * Represents a command to export data.
 */
public class ExportCommand implements Command {
    private final String format;

    /**
     * Constructs an ExportCommand with the specified format.
     *
     * @param format The format to export data in (csv or txt)
     */
    public ExportCommand(String format) {
        // If format is null or empty, default to "csv"
        this.format = (format == null || format.trim().isEmpty()) ? "csv" : format.toLowerCase();
        assert this.format.equals("csv") || this.format.equals("txt") : "Export format must be either 'csv' or 'txt'";
    }

    /**
     * Executes the command to export data.
     *
     * @param transactionManager The transaction manager to export data from
     * @param ui                 The UI to interact with the user
     * @param storage            The storage to save data
     * @return The response message from executing the command
     */
    @Override
    public String execute(TransactionManager transactionManager, Ui ui, Storage storage) {
        assert transactionManager != null : "TransactionManager cannot be null";
        assert storage != null : "Storage cannot be null";
        assert format != null : "Export format cannot be null";
        assert format.equals("csv") || format.equals("txt") : "Export format must be either 'csv' or 'txt'";

        String filePath;
        try {
            if ("txt".equals(format)) {
                filePath = storage.exportToTxt(transactionManager);
            } else {
                // Default to CSV
                filePath = storage.exportToCsv(transactionManager);
            }
            assert filePath != null && !filePath.isEmpty() : "Export file path cannot be null or empty";
            return "Data exported successfully to: " + filePath;
        } catch (Exception e) {
            return "Error exporting data: " + e.getMessage();
        }
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
