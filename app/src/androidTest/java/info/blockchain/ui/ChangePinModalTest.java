package info.blockchain.ui;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import info.blockchain.ui.util.UiUtil;
import info.blockchain.wallet.MainActivity;
import piuk.blockchain.android.R;

public class ChangePinModalTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo = null;

    private static boolean loggedIn = false;

    public ChangePinModalTest() {
        super(MainActivity.class);
    }

    @Override
    public void setUp() throws Exception {

        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());

        if(!loggedIn){
            UiUtil.getInstance(getActivity()).enterPin(solo, solo.getString(R.string.qa_test_pin1));
            try{solo.sleep(4000);}catch (Exception e){}
            loggedIn = true;
        }

        UiUtil.getInstance(getActivity()).openNavigationDrawer(solo);
        try{solo.sleep(500);}catch (Exception e){}
        solo.clickOnText(getActivity().getString(R.string.change_pin_code));
        try{solo.sleep(500);}catch (Exception e){}
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testA_CancelModal() throws AssertionError{

        try{solo.sleep(500);}catch (Exception e){}
        solo.clickOnText(getActivity().getString(R.string.dialog_cancel));
        assertTrue("Cancel pin change modal failed.", !solo.waitForText(getActivity().getString(R.string.dialog_cancel), 1, 500));
    }

    public void testB_EnterInvalidPassword() throws AssertionError{

        try{solo.sleep(500);}catch (Exception e){}
        solo.enterText(solo.getEditText(0),"aaaaaaaaaaaaa");
        solo.clickOnText(getActivity().getString(R.string.ok_cap));
        assertTrue("Invalid password toast did not appear.",solo.waitForText(getActivity().getString(R.string.invalid_password), 1, 500));
    }
}
