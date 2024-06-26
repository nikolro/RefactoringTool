/*
This tests Non-rewritable Overrides the programBefore and programAfter should stay the same,
because c in my method influence c in the overriden function addAll witch is external function.
The c in addAll in class CustomList have edge to the c in the class ArrayList which is external class.
Which means changing our c have to chnage the external c, and we cant do that.
The final variance for c here is BIVARIANT but we cant change.
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