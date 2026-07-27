package org.hit.chiikaiwabe.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

public interface FirebaseService {
    FirebaseToken verifyIdToken(String idToken) throws FirebaseAuthException;
}
