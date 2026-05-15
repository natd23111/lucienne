package Project;

import java.util.Map;

public interface ResourceParser<T> {
    Map<String, T> parse() throws GameDataException;
}