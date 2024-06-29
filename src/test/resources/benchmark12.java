/*
this tests Non-rewritable Overrides the programBefore and programAfter should stay the same,
because c in my method influence c in the overriden function addAll witch is external function.
the c in addAll in class CustomList have edge to the c in the class ArrayList which is external class.
which means changing our c have to chnage the external c; and we cant do that.
 */

class bechmark12 {

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