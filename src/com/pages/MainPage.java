/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.Display;


import java.util.Vector;

import com.mainMIDlet;
import com.NetworkController;
import com.components.HTMLComponentItem;
import com.Utilities;
import com.JsonObject;
import com.components.LinkButton;
/**
 *
 * @author caxthelm
 */
public class MainPage extends BasePage {
    //Common Command Ids ;
    private final int COMMAND_EXIT = COMMAND_RIGHT;
    private final int COMMAND_STOREDPAGES = COMMAND_CENTER;
    private final int COMMAND_SETTINGS = COMMAND_LEFT;
    
    //Lwuit Commands:   
    
    TextField m_cSearchTextField = null;
    Button m_cSearchButton = null;
    public MainPage() {
        super("MainPageForm", PAGE_MAIN);
        
        if(!m_bIsLoaded) {
            //TODO: make error dialog.
            System.err.println("We failed to load");
            return;
        }
        try {
            //Create dynamic components here.
                     
            m_cSearchTextField = (TextField)mainMIDlet.getBuilder().findByName("SearchTextField", m_cHeaderContainer);
            m_cSearchButton = (Button)mainMIDlet.getBuilder().findByName("SearchIconButton", m_cHeaderContainer);            
            if(m_cSearchButton != null) {
                m_cSearchButton.setVisible(false);
            }
            if(m_cSearchTextField != null) {
                m_cSearchTextField.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        TextField myText = (TextField)ev.getComponent();
                        if(!Display.getInstance().editingText) {
                            Display.getInstance().editString(ev.getComponent(), myText.getMaxSize(), myText.getConstraint(), myText.getText());
                        }
                    }
                });
                
                m_cSearchTextField.addDataChangeListener(new DataChangedListener()  {
                    public void dataChanged(int i, int i1) {
                        if(m_cSearchTextField != null) {
                            String message = m_cSearchTextField.getText();
                            if(m_cSearchButton != null) {
                                if(message != null && !message.equalsIgnoreCase(""))
                                {
                                    m_cSearchButton.setVisible(true);
                                }else 
                                    m_cSearchButton.setVisible(false);
                            }                            
                        }
                        m_cForm.repaint();
                    }
                });
            }
            m_cForm.addShowListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    m_cForm.removeShowListener(this);
                    NetworkController.getInstance().fetchArticle(mainMIDlet.getLanguage(), "Main_Page", "0");
                }
            });
            updateSoftkeys();
            m_cForm.setCyclicFocus(false);
            m_cForm.setFocusScrolling(false);
            m_cForm.addCommandListener(this);
            mainMIDlet.getBuilder().setHomeForm("MainPageForm");
            //mForm.repaint();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }//end SearchPage()
    
    public void updateSoftkeys() {
        int i = 0;m_cForm.removeAllCommands();
        String  str = "";
        /*if(!mainMIDlet.isTouchEnabled()) {
            str = mainMIDlet.getString("SearchSK");
            m_cForm.addCommand(new Command(str, Command_Search), i++);
            str = mainMIDlet.getString("OkSK");
            m_cForm.addCommand(new Command(str, COMMAND_OK), i++);
        }else {
        }*/
        str = mainMIDlet.getString("StoredPagesSK");
        m_cForm.addCommand(new Command(str, COMMAND_STOREDPAGES), i++);
        str = mainMIDlet.getString("SettingsSK");
        m_cForm.addCommand(new Command(str, COMMAND_SETTINGS), i++);
        str = mainMIDlet.getString("ExitSK");
        m_cForm.addCommand(new Command(str, COMMAND_EXIT), i++);
        
        /*if(!mainMIDlet.isTouchEnabled()) {            
            str = mainMIDlet.getString("ExitSK");
            m_cForm.addCommand(new Command(str, Command_Exit), i++);
        }*/
    }//end updateSoftkeys()
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action main: " + ae.getCommand().getId());
        int commandId = ae.getCommand().getId();
        if(commandId == COMMAND_OK) {
            Component focusedComp = m_cForm.getFocused();
            if(focusedComp instanceof Button){
                Button test = (Button)focusedComp;
                commandId = test.getCommand().getId();
            }else if(focusedComp instanceof Container) {
                Container testCont = (Container)focusedComp;
                Button test = (Button)testCont.getLeadComponent();
                if(test != null && testCont.getLeadComponent() instanceof Button) {
                    commandId = test.getCommand().getId();
                }
            }
        }
        switch(commandId) {                
            //Softkeys
            case COMMAND_EXIT:
                mainMIDlet.getMIDlet().notifyDestroyed();
                break;
            case COMMAND_STOREDPAGES:
                mainMIDlet.setCurrentPage(new SearchPage());
                break;
            case COMMAND_SETTINGS:
                mainMIDlet.setCurrentPage(new SettingsPage());
                break;
            case COMMAND_SEARCHBUTTON:
                //TODO: Network connection to get "did you mean" items.
                if(m_cSearchButton != null && m_cSearchButton.isVisible()) {
                    String text = "";
                    if(m_cSearchTextField != null) {
                        text = m_cSearchTextField.getText();
                    }
                    if(text.length() > 0) {
                        mainMIDlet.setCurrentPage(new ArticlePage(text, true));
                    }
                }
                break;
            case COMMAND_IMAGE:
                {
                    Component oComp = ae.getComponent();
                    if(oComp instanceof LinkButton) {
                        String url = "http:"+((LinkButton)oComp).getLink();
                        int sizeIdx = url.indexOf("px");//TODO: make this work better without giving errors.
                        
                        System.out.println("url: "+url);
                        mainMIDlet.setCurrentPage(new ImageDialog(
                                ((LinkButton)oComp).getOtherInfo(), ((LinkButton)oComp).getText(), url));
                    }
                }
                break;
            case COMMAND_LINK: //internal links
                {
                    Component oComp = ae.getComponent();
                    if(oComp instanceof LinkButton) {
                        String url = ((LinkButton)oComp).getLink();
                        int wikiIdx = url.indexOf("/wiki/");
                        if(wikiIdx >= 0) {
                            String title = url.substring(wikiIdx + 6);
                            mainMIDlet.setCurrentPage(new ArticlePage(title, false));
                        }
                    }
                }
            default://dealing with the dynamic events
                {
                }
                break;
        }
    } //end actionPerformed(ActionEvent ae)
    
    public void refreshPage() {        
        m_cForm.addShowListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                //Putting the refresh in a show listener to make sure the page is ready to refresh.
                m_cForm.removeShowListener(this);
                checkRefresh();
            }
        });
    }//end refreshPage()
    
    private void checkRefresh() {
        NetworkController.hideLoadingDialog();
        addData(null, NetworkController.PARSE_SEARCH);
        Thread.yield();
        
        super.refreshPage();
    }//end checkRefresh()
    
    public void addData(Object _results, int _iResultType) {
        
        Vector sections = Utilities.getSectionsFromJSON((JsonObject)_results);
        if(m_cContentContainer != null && sections != null && sections.size() > 0)
        {
            Container articleCont = (Container)mainMIDlet.getBuilder().findByName("ArticleBodyTextItem", m_cContentContainer);
            if(articleCont == null) {
                //TODO: Add in error message here.
                return;
            }
            articleCont.removeAll();
            Object oTextItem = sections.firstElement();
            if(oTextItem instanceof JsonObject) {
                String sText = (String)((JsonObject)oTextItem).get("text");
                sText = Utilities.decodeEverything(sText);
                HTMLComponentItem oHTMLItem = new HTMLComponentItem();                
                Component cTextComp = oHTMLItem.createComponent(sText);
                if(cTextComp != null) {
                    articleCont.addComponent(cTextComp);
                }
            }
        }//end if(m_cContentContainer != null && sections != null && sections.size() > 0)
        m_cForm.repaint();
    }//end addData(Object _results)
    
    
}
