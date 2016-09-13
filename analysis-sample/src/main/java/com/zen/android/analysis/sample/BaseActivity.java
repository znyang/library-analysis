package com.zen.android.analysis.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import rx.Observable;

/**
 * @author zen
 * @version 2016/9/13
 */

public abstract class BaseActivity extends AppCompatActivity {

    Observable<?> ready;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBaseCreate(savedInstanceState);
        onAfterCreate(savedInstanceState);
    }

    private void onAfterCreate(@Nullable Bundle state) {
        if (InitManager.isHasInit()) {
            ready = null;
            afterCreate(state);
            return;
        }
        createReady(state);
    }

    private void createReady(Bundle state) {
        if (ready == null) {
            ready = InitManager
                    .ready()
                    .map(result -> {
                        afterCreate(state);
                        return result;
                    });
        }
    }

    protected abstract void afterCreate(@Nullable Bundle state);

    protected abstract void onBaseCreate(@Nullable Bundle savedInstanceState);

    @Override
    protected void onResume() {
        super.onResume();
        if (ready == null) {
            onObsResume();
        } else {
            Observable<?> obs = genObsResume();
            if (obs != null) {
                Observable.concat(ready, obs).subscribe();
            } else {
                ready.subscribe();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ready = null;
    }

    protected Observable<?> genObsResume() {
        return Observable.create(subscriber -> {
            onObsResume();
            subscriber.onNext(null);
            subscriber.onCompleted();
        });
    }

    protected void onObsResume() {
    }
}
