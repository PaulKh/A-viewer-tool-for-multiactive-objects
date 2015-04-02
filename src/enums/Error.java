package enums;

/**
 * Created by pkhvoros on 3/25/15.
 */
public enum Error {
    RequestNeverEnds("Request never ends.", IssueType.Error),
    WreckedFile("Wrecked file.", IssueType.Error),
    WrongFileFormat("Wrong file format.", IssueType.Warning);
    private String message;
    private IssueType issueType;

    Error(String error, IssueType issueType) {
        this.message = error;
        this.issueType = issueType;
    }

    public String getMessage() {
        return message;
    }

    public IssueType getIssueType() {
        return issueType;
    }
}
