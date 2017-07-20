package jukebox.jukebox;

// Interface for callbacks. Because Google was not able to implement these 4 lines of code for platform versions < 23 :/
public interface Function<T, E>
{
    E apply(T t);
}
