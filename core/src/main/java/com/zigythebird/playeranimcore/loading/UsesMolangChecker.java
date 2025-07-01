package com.zigythebird.playeranimcore.loading;

public class UsesMolangChecker {
    private boolean usesMolang;

    public void molangHasBeenUsed() {
        usesMolang = true;
    }

    public boolean hasMolangBeenUsed() {
        return usesMolang;
    }
}
