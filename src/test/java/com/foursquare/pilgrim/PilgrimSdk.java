package com.foursquare.pilgrim;

import android.content.Context;

public class PilgrimSdk {

    public static void with(PilgrimSdk.Builder builder) {
        //do nothing
    }


        public static class Builder {
            public Builder(Context context) {

            }

            public Builder consumer(String a, String b) {
                return this;
            }

            public Builder logLevel(LogLevel logLevel) {
                return this;
            }

            public Builder enableDebugLogs() {
                return this;
            }
        }
}
