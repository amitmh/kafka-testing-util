package com.am

package object it {

  implicit class Ops[A](a: A) {
    def pipe[B](fab: A => B): B = fab(a)

    def tap[B](fab: A => B): A = fab(a).pipe(_ => a)
  }

}
