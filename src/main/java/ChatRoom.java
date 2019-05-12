import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatRoom {

    private List<Connection> participants;
    private String id;

    public ChatRoom(Connection... params) {
        participants = new ArrayList<Connection>(Arrays.asList(params));
        id = Long.toString((new Date()).getTime());
    }
    public void addToPartners(Connection con){
        for(Connection con2:participants){
            if(!con2.equals(con))
            con2.room=this;
        }
    }

    public Connection leaveChatRoom(Connection con) {
        if (participants.size() > 2) {
            participants.remove(con);
            con.room=null;
            return null;
        } else {
            Server.rooms.remove(this);
            for(Connection con2:participants){
                con2.room=null;
            }
            participants.remove(con);
            return participants.get(0);
        }
    }
    public void printMessage(Connection con,String str){
        for(Connection con2:participants){
            if(!con2.equals(con)) {
                con2.out.println(con.name + ": " + str);
            }
        }
    }
}
