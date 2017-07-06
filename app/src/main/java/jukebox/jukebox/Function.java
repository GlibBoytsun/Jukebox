package jukebox.jukebox;

public interface Function<T, E>
{
    E apply(T t);
}
