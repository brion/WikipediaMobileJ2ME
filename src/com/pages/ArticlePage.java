/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pages;

import com.*;
import com.sun.lwuit.*;
import com.sun.lwuit.events.*;
import com.sun.lwuit.Display;
import com.sun.lwuit.html.DefaultHTMLCallback;
import com.sun.lwuit.html.HTMLComponent;


import java.util.Vector;

/**
 *
 * @author caxthelm
 */
public class ArticlePage extends BasePage {
    //Common Command Ids ;
    private final int COMMAND_BACK = COMMAND_RIGHT;
    private final int COMMAND_SEARCH = COMMAND_CENTER;
    private final int COMMAND_SAVEPAGE = COMMAND_LEFT;
    private final int COMMAND_BOOKMARK = COMMAND_SAVEPAGE + 1;
    //private final int Command_Privacy = Command_Terms + 1;
    private final int COMMAND_HOME = COMMAND_BOOKMARK + 1;
    
    //Lwuit Commands:   
    JsonObject m_oData = null;
    String m_sTitle = "";
    String m_sCurrentSections = "0";
    TextField searchTextField = null;
    public ArticlePage(String _sTitle, JsonObject _oData) {
        super("ArticlePageForm", PAGE_MAIN);
        
        if(!m_bIsLoaded) {
            //TODO: make error dialog.
            System.err.println("We failed to load");
            return;
        }
        m_oData = _oData;
        m_sTitle = _sTitle;
        try {
            //Create dynamic components here.
            Label cTitleLabel = (Label)mainMIDlet.getBuilder().findByName("SubjectTitleLabel", m_cHeaderContainer);
            if(cTitleLabel != null) {
                String realTitle = _sTitle.replace('_', ' ');
                cTitleLabel.setText(realTitle);
            }
            
            m_cForm.addShowListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    m_cForm.removeShowListener(this);
                    if(m_oData == null) {
                        NetworkController.getInstance().performSearch(m_sTitle,  "0");
                    }else {
                        addData(m_oData);
                    }
                }
            });
            updateSoftkeys();
            m_cForm.addCommandListener(this);
            //mForm.repaint();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }//end SearchPage()
    
    public void updateSoftkeys() {
        int i = 0;
        if(true){
            return;
        }
        m_cForm.removeAllCommands();
        String  str = "";
        str = mainMIDlet.getString("ExitSK");
        m_cForm.addCommand(new Command(str, COMMAND_BACK), i++);
        str = mainMIDlet.getString("SearchSK");
        m_cForm.addCommand(new Command(str, COMMAND_SEARCH), i++);
        str = mainMIDlet.getString("SavePageSK");
        m_cForm.addCommand(new Command(str, COMMAND_SAVEPAGE), i++);
        str = mainMIDlet.getString("BookMarkSK");
        m_cForm.addCommand(new Command(str, COMMAND_BOOKMARK), i++);
        //str = mainMIDlet.getString("PrivacySK");
        //mForm.addCommand(new Command(str, Command_Privacy), Command_Privacy);
        str = mainMIDlet.getString("HomeSK");
        m_cForm.addCommand(new Command(str, COMMAND_HOME), i++);
        
    }//end updateSoftkeys()
    
    public void actionPerformed(ActionEvent ae) {
        System.err.println("Action article: " + ae.getCommand().getId());
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
            case COMMAND_BACK:
                    mainMIDlet.pageBack();
                break;
            case COMMAND_SEARCH:
                    mainMIDlet.setCurrentPage(new SearchPage());
                break;
            case COMMAND_SAVEPAGE:
                    //mainMIDlet.setCurrentPage(new SearchPage());
                break;
            case COMMAND_BOOKMARK:
                //mainMIDlet.setCurrentPage(new WebViewDialog("TermsUrl"));
                break;
            case COMMAND_HOME:
                    mainMIDlet.setCurrentPage(new MainPage(), true);
                break;
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
        addData(null);
        Thread.yield();
        
        super.refreshPage();
    }//end checkRefresh()
    
    public void addData(Object _results) {
        if(_results == null) {
            //We have nothing, make the data call.
            NetworkController.getInstance().performSearch(m_sTitle, m_sCurrentSections);
        }
        Vector sections = Utilities.getSectionsFromJSON((JsonObject)_results);
        if(m_cContentContainer != null && sections != null && sections.size() > 0)
        {
            m_cContentContainer.removeAll();
            //Deal with the main article text first.
            Object oTextItem = sections.firstElement();
            if(oTextItem instanceof JsonObject) {
                String sText = (String)((JsonObject)oTextItem).get("text");
                sText = Utilities.stripSlash(sText);
                HTMLComponentItem oHTMLItem = new HTMLComponentItem(sText);
                HTMLComponent cTextComp = (HTMLComponent)oHTMLItem.getComponent();
                if(cTextComp != null) {
                    cTextComp.setHTMLCallback(new DefaultHTMLCallback()
                    {
                        public boolean linkClicked(HTMLComponent htmlC, java.lang.String url) 
                        {
                            System.out.println("link: "+url);
                            int wikiIdx = url.indexOf("/wiki/");
                            if(wikiIdx >= 0) {
                                String title = url.substring(wikiIdx + 6);
                                mainMIDlet.setCurrentPage(new ArticlePage(title, null));
                            }
                            return false;
                        }

                    }); 
                    m_cContentContainer.addComponent(cTextComp);
                }
            }//end if(oTextItem instanceof JsonObject)
            
            //Add in the other sections
                System.out.println("sections: "+sections.size());
            for(int i = 1; i < sections.size(); i++) {
                JsonObject oSection = (JsonObject)sections.elementAt(i);
                String sTitle = (String)oSection.get("line");
                String sText = (String)oSection.get("text");
                boolean bActive = false;
                if(sText != null && sText.length() > 0) {
                    bActive = false;
                }
                System.out.println("item: "+sTitle);
                Integer sTocLevel = (Integer)oSection.get("toclevel");
                String sID = (String)oSection.get("id");
                if(sTocLevel.intValue() == 1) {
                    SectionComponentItem sectionItem = new SectionComponentItem(sTitle, 40 + i, sID);
                    Component cSectionComp = sectionItem.createComponent(sTitle, bActive);
                    if(cSectionComp != null) {
                        m_cContentContainer.addComponent(cSectionComp);
                    }
                }
            }//end for(int i = 1; i > sections.size(); i++)
        }//end if(m_cContentContainer != null && sections != null && sections.size() > 0)
        m_cForm.repaint();
    }//end addData(Object _results)
    
    
}
