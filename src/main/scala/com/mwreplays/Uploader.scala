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

import swing._
import dispatch.classic._
import dispatch.classic.mime._
import Mime._
import swing.FileChooser.Result._
import swing.FileChooser.SelectionMode._
import javax.swing.filechooser.FileNameExtensionFilter
import BorderPanel.Position._
import java.io.File

object Uploader extends SimpleSwingApplication {

    def top = new MainFrame {
        title = "mwreplays.com Uploader"
        contents = new BorderPanel {
            // Define Step 1 Panel
            val step1 = new GridPanel(2, 2) {
                val username = new TextField
                val password = new PasswordField
                contents.append(new Label("Username:"), username, new Label("Password:"), password)
            }
            // Define Step 2 Panel
            val step2 = new BorderPanel {
                val chooser = new FileChooser {
                    title = "Select Replays for Upload"
                    multiSelectionEnabled = true
                    fileSelectionMode = FilesOnly
                    fileHidingEnabled = true
                    fileFilter = new FileNameExtensionFilter("WoT Replays", "replay")
                }
                case class FileItem(name: String)
                val fileList: ListView[File] = new ListView
                layout(new Button(Action("Select Replays for Upload") {
                    if(chooser.showOpenDialog(this) == Approve) fileList.listData = for(f <- chooser.selectedFiles) yield f
                })) = North
                layout(new ScrollPane(fileList)) = Center
            }
            //TODO: Define Step 3 Panel
            val step3 = new BorderPanel {

            }
            // Define CardPanel
            val cards = new CardPanel {
                add(step1, "step1")
                add(step2, "step2")
                add(step3, "step3")
            }
            var currentCard = "step1"
            // Define Step Buttons Panel
            val navigation = new FlowPanel {
                // Define Actions
                val backAction: Action = Action("Back") {
                    currentCard match {
                        case "step2" =>
                            cards.show("step1")
                            currentCard = "step1"
                            back.action = exitAction
                        case "step3" =>
                            cards.show("step2")
                            currentCard = "step2"
                            next.action = nextAction
                        case _ =>
                    }
                }
                val nextAction: Action = Action("Next") {
                    currentCard match {
                        case "step1" =>
                            cards.show("step2")
                            currentCard = "step2"
                            back.action = backAction
                        case "step2" =>
                            cards.show("step3")
                            currentCard = "step3"
                            next.action = uploadAction
                        case _ =>
                    }
                }
                val uploadAction: Action = Action("Upload") {
                    //TODO: Implement
                }
                val exitAction: Action = Action("Exit") {
                    System.exit(0)
                }
                // Define Buttons
                val back: Button = new Button(exitAction)
                val next: Button = new Button(nextAction)
                // Layout
                contents.append(back, next)
            }
            // Layout Panels
            layout(cards) = Center
            layout(navigation) = South
        }
    }
}
