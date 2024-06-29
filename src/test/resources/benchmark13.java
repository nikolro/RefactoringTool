//this tests Non-rewritable Overrides the programBefore and programAfter should stay the same
//because c in my method influence c in the overriden function addAll witch is external function
class bechmark13 {

    class programBefore {
        import java.util.ArrayList;
        import java.util.Collection;

        class CustomList<E> extends ArrayList<E> {
            @Override
            public boolean addAll(Collection<? extends E> c) {
                return true;
            }

        }
    }

    class programAfter {
        import java.util.ArrayList;
        import java.util.Collection;

        class CustomList<E> extends ArrayList<E> {
            @Override
            public boolean addAll(Collection<? extends E> c) {
                return true;
            }

        }
    }

}