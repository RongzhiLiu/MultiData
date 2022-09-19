package com.lrz.annotation.simple.data;

import com.google.gson.annotations.SerializedName;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

@Table(name = "text_config")
public class TextConfig {
    @SerializedName("book_id")
    protected String bookId;

    @Get(name = "bookId",defaultString = "1213")
    public String getBookId() {
        return bookId;
    }

    @Set(name = "bookId")
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }


}
