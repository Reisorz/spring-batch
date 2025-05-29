package com.mls.spring_batch.faultTolerant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
public class CustomSkipPolicy implements SkipPolicy {

    private final Integer SKIPLIMIT = 3;


    @Override
    public boolean shouldSkip(Throwable exception, long skipCount) throws SkipLimitExceededException {

        if (exception instanceof FileNotFoundException) {
            return false;
        } else if (exception instanceof FlatFileParseException && skipCount < SKIPLIMIT) {

            FlatFileParseException flatFileParseException = (FlatFileParseException) exception;
            String input = flatFileParseException.getInput();
            int lineNumber = flatFileParseException.getLineNumber();
            System.out.println("Error parsing file with input " + input + ", at line " + lineNumber + ":");

            return true;
        }

        return true;
    }
}