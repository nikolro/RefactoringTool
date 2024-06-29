//mix pro max
class bechmark14 {

    class programBefore {
        import java.util.Iterator;
        import java.util.List;

        class Animal {
        }

        class Dog extends Animal {
        }

        class Main {
            public void printAnimals(List<Animal> animals) {
                Iterator<Animal> iterator = animals.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }
        }
    }

    class programAfter {
        import java.util.Iterator;
        import java.util.List;

        class Animal {
        }

        class Dog extends Animal {
        }

        class Main {
            public void printAnimals(List<? extends Animal> animals) {
                Iterator<? extends Animal> iterator = animals.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }
        }
    }

}