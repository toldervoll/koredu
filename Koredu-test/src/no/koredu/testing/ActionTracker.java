package no.koredu.testing;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ActionTracker {

  private final List<String> actions = Lists.newArrayList();

  public void track(String from, String action, String to) {
    String actionDescription = from + " -> " + action + " -> " + to;
    System.out.println(actionDescription);
    actions.add(actionDescription);
  }

  public List<String> getActions() {
    return actions;
  }

}
