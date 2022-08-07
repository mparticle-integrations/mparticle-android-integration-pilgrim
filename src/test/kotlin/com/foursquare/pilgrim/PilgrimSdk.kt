package com.foursquare.pilgrim

import android.content.Context

class PilgrimSdk {

    companion object {
        fun with(builder: Builder?) {
            //do nothing
        }
    }


    class Builder(context: Context?) {
        fun consumer(a: String?, b: String?): Builder {
            return this
        }

        fun logLevel(logLevel: LogLevel?): Builder {
            return this
        }

        fun enableDebugLogs(): Builder {
            return this
        }
    }
}
