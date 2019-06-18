package cep;

import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CEPListener implements UpdateListener {
    private String id;
    public CEPListener(String id) {
        this.id = id;
    }
    public void update(EventBean[] newData, EventBean[] oldData) {
        if (id.equalsIgnoreCase("ife")) {
            System.out.println("(" + id + ") " + "Event matched: " + newData[0].getUnderlying());
        }
    }
}
