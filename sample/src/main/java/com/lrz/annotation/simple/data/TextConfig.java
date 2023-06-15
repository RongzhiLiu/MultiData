package com.lrz.annotation.simple.data;

import com.google.gson.annotations.SerializedName;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Set;
import com.lrz.multi.annotation.Table;

import java.util.List;

/**
 * class 类型的表
 */
@Table(name = "text_config")
public class TextConfig {
    @SerializedName("book_id")
    protected String bookId;
    protected List<String> strings;

    @Get(name = "bookId")
    public String getBookId() {
        return bookId;
    }

    @Set(name = "bookId")
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    @Get(name = "strings")
    public List<String> getStrings() {
        return strings;
    }

    @Set(name = "strings")
    public void setStrings(List<String> strings) {
        this.strings = strings;
    }
}
