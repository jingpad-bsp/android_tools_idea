import java.util.Collection;

class Foo {

  {
    Collection<Foo> foos;
    Foo[] f = foos.toArray(new Foo[foos.size()]);<caret>
  }

}
