import java.io.Serializable;

public class ChatMessage implements Serializable {

    private String SenderName;
    private String receiverName;
    private Object data;

    public String getSenderName() {
        return SenderName;
    }

    public void setSenderName(String SenderName) {
        this.SenderName = SenderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return data.toString();
    }

    public void setMessage(String msg) {
        this.data = msg;
    }

}
