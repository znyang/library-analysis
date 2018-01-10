package com.zen.android.analysis.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * @author zen
 * @version 2016/9/13
 */

public abstract class BaseActivity extends Activity {

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
                    .map(new Func1<Object, Object>() {
                        @Override
                        public Object call(Object result) {
                            afterCreate(state);
                            return result;
                        }
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
        return Observable.create(new OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                BaseActivity.this.onObsResume();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        });
    }

    protected void onObsResume() {
    }
}
