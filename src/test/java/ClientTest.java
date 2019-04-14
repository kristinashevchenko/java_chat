import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class ClientTest {
    @Test
    public void testCheckInvalidInfo() throws Exception {
        String[] arr = {"/registr"};
        boolean isOk = Client.checkInvalidInfo(arr);
        assertTrue(isOk);

        arr=new String[3];
        arr[0]="/register";arr[1]="agen";arr[2]="Cooper";
       isOk = Client.checkInvalidInfo(arr);
        assertTrue(isOk);

        arr=new String[3];
        arr[0]="/registe";arr[1]="agen";arr[2]="Cooper";
        isOk = Client.checkInvalidInfo(arr);
        assertTrue(isOk);

        arr=new String[3];
        arr[0]="/register";arr[1]="client";arr[2]="Cooper";
        isOk = Client.checkInvalidInfo(arr);
        assertTrue(!isOk);

        arr=new String[4];
        arr[0]="/register";arr[1]="agent";arr[2]="Cooper";arr[3]="2";
        isOk = Client.checkInvalidInfo(arr);
        assertTrue(!isOk);
    }
}
