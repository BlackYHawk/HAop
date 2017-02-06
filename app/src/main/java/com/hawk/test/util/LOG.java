package com.hawk.test.util;

import android.util.Log;

public class LOG {
	private static final String TAG = "hope";
	private static final boolean D = true;

	public static final void D(String debug) {
		if (D)
			Log.d(TAG, debug);
	}

	public static final void D(String tag, String debug) {
		if (D) {
			Log.d(tag, debug);
		}
	}

    public static final void E(String debug) {
        if (D)
            Log.e(TAG, debug);
    }

    public static final void E(String tag, String debug) {
        if (D) {
            Log.e(tag, debug);
        }
    }

	public static final void Exec(Exception e) {
		if (D) {
			e.printStackTrace();
		}
	}

}
