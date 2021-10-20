package fr.afaucogney.mobile.flipper.internal.util.rx

import io.reactivex.rxjava3.observers.DisposableObserver
import timber.log.Timber

open class RxLogSubscriber<T>(private var tag: String) : DisposableObserver<T>() {

    override fun onStart() {
        super.onStart()
        Timber.tag("_$tag").i("onStart")
    }

    override fun onNext(t: T) {
        Timber.i("@@Thread : ${Thread.currentThread().name}")
        if (t is List<*>) {
            Timber.tag("_$tag").v("onNext with : ${t.count()} items")
        } else {
            Timber.tag("_$tag").v("onNext with : $t")
        }
    }

    override fun onError(e: Throwable) {
        Timber.tag("_$tag").e("onError with : $e")
    }

    override fun onComplete() {
        Timber.tag("_$tag").i("onComplete")
    }
}
