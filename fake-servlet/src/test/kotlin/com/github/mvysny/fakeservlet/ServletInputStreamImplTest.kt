package com.github.mvysny.fakeservlet

import javax.servlet.ReadListener
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import kotlin.test.expect

class ServletInputStreamImplTest {
    private val stream: ServletInputStreamImpl = ServletInputStreamImpl(ByteArrayInputStream("Hello".toByteArray()))

    @Test fun read() {
        expect(false) { stream.isFinished }
        expect(true) { stream.isReady }
        expect(5) { stream.available() }
        expect('H'.code) { stream.read() }
        val buf = ByteArray(2)
        expect(2) { stream.read(buf) }
        expect("el") { buf.decodeToString() }
        expect(2) { stream.read(buf, 0, 2) }
        expect("lo") { buf.decodeToString() }
        expect(true) { stream.isFinished }
        expect(0) { stream.available() }
        expect(-1) { stream.read() }
    }

    @Test fun skip() {
        expect(3) { stream.skip(3) }
        expect("lo") { stream.readBytes().decodeToString() }
    }

    @Test fun markAndReset() {
        expect(true) { stream.markSupported() }
        stream.read()
        stream.mark(10)
        expect("ello") { stream.readBytes().decodeToString() }
        stream.reset()
        expect("ello") { stream.readBytes().decodeToString() }
        stream.close()
    }

    @Test fun `readLine() from ServletInputStream`() {
        val s = ServletInputStreamImpl(ByteArrayInputStream("ab\ncd".toByteArray()))
        val buf = ByteArray(10)
        expect(3) { s.readLine(buf, 0, buf.size) }
        expect("ab\n") { buf.decodeToString(0, 3) }
    }

    @Test fun `setReadListener() fails since async is never started`() {
        assertThrows<IllegalStateException> {
            stream.setReadListener(object : ReadListener {
                override fun onDataAvailable() {}
                override fun onAllDataRead() {}
                override fun onError(t: Throwable) {}
            })
        }
    }
}
