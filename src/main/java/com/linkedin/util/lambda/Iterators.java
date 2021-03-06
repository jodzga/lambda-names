package com.linkedin.util.lambda;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.objectweb.asm.tree.AbstractInsnNode;

class Iterators {

  static <E> Stream<E> toStream(Iterator<E> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
  }

  static <E> Iterator<E> filter(Iterator<E> it, Predicate<E> p) {
    return new Iterator<E>() {

      private E _next;

      @Override
      public boolean hasNext() {
        if (_next != null) {
          return true;
        } else {
          while(true) {
            if (it.hasNext()) {
              E next = it.next();
              if (p.test(next)) {
                _next = next;
                return true;
              }
            } else {
              return false;
            }
          }
        }
      }

      @Override
      public E next() {
        if (hasNext()) {
          E current = _next;
          _next = null;
          return current;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  static <E> Iterator<E> until(Iterator<E> it, Predicate<E> p) {
    return new Iterator<E>() {

      private E _next;
      private boolean _done = false;

      @Override
      public boolean hasNext() {
        if (_done) {
          return false;
        }
        if (_next != null) {
          return true;
        } else {
          if (it.hasNext()) {
            _next = it.next();
            if (p.test(_next)) {
              _done = true;
              return false;
            } else {
              return true;
            }
          } else {
            return false;
          }
        }
      }

      @Override
      public E next() {
        if (hasNext()) {
          E current = _next;
          _next = null;
          return current;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  static Iterator<AbstractInsnNode> forwardIterator(AbstractInsnNode instruction) {
    return iterator(instruction, AbstractInsnNode::getNext);
  }

  static Iterator<AbstractInsnNode> backwardIterator(AbstractInsnNode instruction) {
    return iterator(instruction, AbstractInsnNode::getPrevious);
  }

  static Iterator<AbstractInsnNode> iterator(AbstractInsnNode instruction,
      Function<AbstractInsnNode, AbstractInsnNode> move) {
    return new Iterator<AbstractInsnNode>() {

      private AbstractInsnNode _current = null;
      private AbstractInsnNode _next = instruction;

      @Override
      public boolean hasNext() {
        if (_next != null) {
          return true;
        } else {
          _next = move.apply(_current);
          return (_next != null);
        }
      }

      @Override
      public AbstractInsnNode next() {
        if (hasNext()) {
          _current = _next;
          _next = null;
          return _current;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

}
