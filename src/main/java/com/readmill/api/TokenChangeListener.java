package com.readmill.api;

/**
 * Notifies listeners that the wrapper's token has changed.
 */
public interface TokenChangeListener {
  /**
   * Triggered when the token has changed in the wrapper.
   * @param newToken The new token (or null if the token was cleared)
   */
  public void onTokenChanged(Token newToken);
}
