package com.mwreplays

/**
 * Created with IntelliJ IDEA.
 * User: adamj
 * Date: 1/3/13
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
/*
This file is part of The mwreplays.com Uploader.

The mwreplays.com Uploader is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The mwreplays.com Uploader is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with The mwreplays.com Uploader.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 This implementation of CardPanel courtesy of Chris Marshall. See https://issues.scala-lang.org/browse/SI-3933
 */
import swing.{Component, LayoutContainer, Panel}
import java.awt.CardLayout

class CardPanel extends Panel with LayoutContainer {
    type Constraints = String
    def layoutManager = peer.getLayout.asInstanceOf[CardLayout]
    override lazy val peer = new javax.swing.JPanel(new CardLayout) with SuperMixin

    private var cards : Map[String, Component] = Map.empty

    protected def areValid(c: Constraints) = (true, "")
    protected def add(comp: Component, l: Constraints) = {
        // we need to remove previous components with the same constraints as the new one,
        // otherwise the layout manager loses track of the old one
        cards.get(l).foreach { old => cards -= l; peer.remove(old.peer) }
        cards += (l -> comp)
        peer.add(comp.peer, l)
    }

    def show(l : Constraints) = layoutManager.show(peer, l)

    def next() { layoutManager.next(peer) }

    def previous() { layoutManager.previous(peer) }

    protected def constraintsFor(comp: Component) = cards.iterator.find { case (_, c) => c eq comp}.map(_._1).orNull
}

