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
import java.io._
import java.util.regex.Pattern
import scala.actors._
import java.awt.datatransfer._;
import java.awt.Toolkit;

//TODO: Fix all UI alignments and sizes
object Uploader extends SimpleSwingApplication {

    val uploadKeyPattern = Pattern.compile("""stopUpload\(\d,'','([^']+)'\)""")

    def top = new MainFrame {
        title = "mwreplays.com Uploader"
        preferredSize = new Dimension(800, 200)
        contents = new BorderPanel {
            // Define Step 1 Panel: Login Details
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
                    fileFilter = new FileNameExtensionFilter("WoT Replays", "wotreplay")
                }
                case class FileItem(name: String)
                val fileList: ListView[File] = new ListView
                layout(new Button(Action("Select Replays for Upload") {
                    if(chooser.showOpenDialog(this) == Approve) fileList.listData = for(f <- chooser.selectedFiles) yield f
                })) = North
                layout(new ScrollPane(fileList)) = Center
            }

            // Define Step 3 Panel
            val uploadStatus = new TextArea {
                editable = false
            }
            val step3 = new ScrollPane(uploadStatus)

            // Define Step 4 Panel
            val linksView: ListView[String] = new ListView
            val step4 = new ScrollPane(linksView)

            // Define Step Panel
            val step = new Label("Login Details")

            // Define CardPanel
            val cards = new CardPanel {
                add(step1, "step1")
                add(step2, "step2")
                add(step3, "step3")
                add(step4, "step4")
            }
            var currentCard = "step1"

            // Define Step Buttons Panel
            val navigation = new FlowPanel {
                // Define Actions
                val backAction: Action = Action("Back") {
                    cards.previous()
                    currentCard match {
                        case "step2" =>
                            currentCard = "step1"
                            back.action = exitAction
                        case "step3" =>
                            currentCard = "step2"
                            next.action = nextAction
                        case _ =>
                    }
                }
                val nextAction: Action = Action("Next") {
                    cards.next()
                    currentCard match {
                        case "step1" =>
                            currentCard = "step2"
                            back.action = backAction
                            step.text = "Select Replays"
                        case "step2" =>
                            currentCard = "step3"
                            next.action = uploadAction
                            step.text = "Upload Replays"
                        case _ =>
                    }
                }

                /* The actual upload needs to happen in a separate thread so as to allow the status.append operation to work properly */
                class UploadActor extends Actor {
                    def act() { loop { react {
                        case "UPLOAD" =>
                            // Init
                            val http = new Http
                            // Login
                            uploadStatus.append("Logging in...\n")
                            val loginRequest = url("http://mwreplays.com/login.php?act=login") << Map("username" -> step1.username.text, "password" -> new String(step1.password.password), "remlog" -> "0")
                            val loginFailed = http(loginRequest >- { responseString => responseString contains "http://mwreplays.com/login.php" })
                            if(loginFailed) {
                                //TODO: Fix Dialog Size, Style and Spacing
                                val dialog = new Dialog(top)
                                dialog.open()
                                dialog.contents = new BorderPanel {
                                    layout(new Label("Unable to login. Check username, password and network connection")) = Center
                                    layout(Button("Ok") { dialog.close() }) = South
                                }
                            } else {
                                // Upload
                                val uploadRequest = url("http://mwreplays.com/upload_frame.php?teg=up")
                                linksView.listData = for(file <- step2.fileList.listData) yield {
                                    uploadStatus.append("Uploading %s..." format file)
                                    http(uploadRequest <<* ("file", file) >- { responseString =>
                                        val matcher = uploadKeyPattern.matcher(responseString)
                                        if(matcher.find()) {
                                            uploadStatus.append("Complete!\n")
                                            "http://mwreplays.com/replay/%s" format matcher.group(1)
                                        }
                                        else {
                                            uploadStatus.append("Failed!\n")
                                            "Unable to upload %s" format file
                                        }
                                }) }
                                // Finish
                                back.action = copyLinksAction
                                next.action = exitAction
                                back.enabled = true
                                next.enabled = true
                                cards.next()
                                step.text = "Upload Results"
                                exit()
                            } }
                        }
                    }
                }

                val uploadAction: Action = Action("Upload") {
                    back.enabled = false
                    next.enabled = false
                    val upload = new UploadActor
                    upload.start()
                    upload ! "UPLOAD"
                }
                val copyLinksAction: Action = Action("Copy Links to Clipboard") {
                    Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new StringSelection(linksView.listData.mkString("\n")), null)
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
            layout(step) = North
            layout(cards) = Center
            layout(navigation) = South

        }
    }
}
