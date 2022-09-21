package com.lrz.annotation.simple.data;

import com.google.gson.annotations.SerializedName;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

@Table(name = "text_config2")
public class TextConfig2 extends TextConfig{
    @SerializedName("type")
    int type = 0;

    @Get(name = "type",defaultInt = 12)
    public int getType() {
        return type;
    }

    @Set(name = "type")
    public void setType(int type) {
        this.type = type;
    }

    @Table(name = "text_config4")
    public static class TextConfig4{
        @SerializedName("type")
        int type = 0;

        @Get(name = "type",defaultInt = 12)
        public int getType() {
            return type;
        }

        @Set(name = "type")
        public void setType(int type) {
            this.type = type;
        }
    }
}
