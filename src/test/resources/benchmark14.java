//mix pro max
/*
Same explanation as benchmark 13.
But we here use class that we declare as type argument ,this only affect the the variance analysis
here we dont have defition site variance to join to the animals use site variance.when coputing the final variance.
 */
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