package com.example.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public class RedisSocketDemo {
	static int count;
	static byte[] buf = new byte[99999];
	static byte[] inbuf = new byte[8192];
	static int incount, limit;

	private final static int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999,
			Integer.MAX_VALUE };
	private final static byte[] DigitTens = { '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1',
			'1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3',
			'3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5',
			'5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7',
			'7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9',
			'9', };

	private final static byte[] DigitOnes = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', };

	private final static byte[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static final byte DOLLAR_BYTE = '$';
	public static final byte ASTERISK_BYTE = '*';
	public static final byte PLUS_BYTE = '+';
	public static final byte MINUS_BYTE = '-';
	public static final byte COLON_BYTE = ':';

	public static void main(String[] args) {
		/*
		 * String uuid = UUID.randomUUID().toString().replace("-", ""); Jedis jedis =
		 * new Jedis("127.0.0.1", 6379); //jedis.set("firstTest", uuid);
		 * System.out.println(jedis.get("firstTest")); jedis.close();
		 */

		try {
			Socket socket = new Socket("127.0.0.1", 6379);
			OutputStream os = socket.getOutputStream();
			byte[] commond = encode(Command.GET.name());
			byte[] arg = encode("firstTest");
			write(os, (byte) '*');
			writeIntCrLf(os, 1 + 1);
			write(os, (byte) '$');
			writeIntCrLf(os, commond.length);
			write(os, commond);
			writeCrLf(os);

			//
			write(os, (byte) '$');
			writeIntCrLf(os, arg.length);
			write(os, arg);
			writeCrLf(os);
			// socket.shutdownOutput();
			flushBuffer(os);
			os.flush();
			//
			InputStream is = socket.getInputStream();

			byte[] result = getBinaryBulkReply(is);

			String encode = encode(result);
			System.out.println(encode);

			is.close();
			os.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static byte[] getBinaryBulkReply(InputStream is) {
		final byte b = readByte(is);
		if (b == PLUS_BYTE) {
			return processStatusCodeReply(is);
		} else if (b == DOLLAR_BYTE) {
			return processBulkReply(is);
		} else {
			throw new JedisConnectionException("Unknown reply: " + (char) b);
		}
	}

	public static int read(InputStream is, byte[] b, int off, int len) throws JedisConnectionException {
		ensureFill(is);

		final int length = Math.min(limit - incount, len);
		System.arraycopy(inbuf, incount, b, off, length);
		incount += length;
		return length;
	}

	private static byte[] processBulkReply(InputStream is) {
		final int len = readIntCrLf(is);
		if (len == -1) {
			return null;
		}

		final byte[] read = new byte[len];
		int offset = 0;
		while (offset < len) {
			final int size = read(is, read, offset, (len - offset));
			if (size == -1)
				throw new JedisConnectionException("It seems like server has closed the connection.");
			offset += size;
		}

		// read 2 more bytes for the command delimiter
		readByte(is);
		readByte(is);

		return read;
	}

	private static int readIntCrLf(InputStream is) {
		return (int) readLongCrLf(is);
	}

	private static long readLongCrLf(InputStream is) {
		final byte[] buf = inbuf;

		ensureFill(is);

		final boolean isNeg = buf[incount] == '-';
		if (isNeg) {
			++incount;
		}

		long value = 0;
		while (true) {
			ensureFill(is);

			final int b = buf[incount++];
			if (b == '\r') {
				ensureFill(is);

				if (buf[incount++] != '\n') {
					throw new JedisConnectionException("Unexpected character!");
				}

				break;
			} else {
				value = value * 10 + b - '0';
			}
		}

		return (isNeg ? -value : value);
	}

	private static byte[] processStatusCodeReply(InputStream is) {

		ensureFill(is);

		int pos = incount;
		final byte[] buf = inbuf;
		while (true) {
			if (pos == limit) {
				return readLineBytesSlowly(is);
			}

			if (buf[pos++] == '\r') {
				if (pos == limit) {
					return readLineBytesSlowly(is);
				}

				if (buf[pos++] == '\n') {
					break;
				}
			}
		}

		final int N = (pos - incount) - 2;
		final byte[] line = new byte[N];
		System.arraycopy(buf, incount, line, 0, N);
		incount = pos;
		return line;
	}

	private static byte[] readLineBytesSlowly(InputStream is) {
		ByteArrayOutputStream bout = null;
		while (true) {
			ensureFill(is);

			byte b = inbuf[incount++];
			if (b == '\r') {
				ensureFill(is); // Must be one more byte

				byte c = inbuf[incount++];
				if (c == '\n') {
					break;
				}

				if (bout == null) {
					bout = new ByteArrayOutputStream(16);
				}

				bout.write(b);
				bout.write(c);
			} else {
				if (bout == null) {
					bout = new ByteArrayOutputStream(16);
				}

				bout.write(b);
			}
		}

		return bout == null ? new byte[0] : bout.toByteArray();
	}

	public static byte readByte(InputStream is) throws JedisConnectionException {
		ensureFill(is);
		return inbuf[incount++];
	}

	private static void ensureFill(InputStream is) throws JedisConnectionException {
		if (incount >= limit) {
			try {
				limit = is.read(inbuf);
				incount = 0;
				if (limit == -1) {
					throw new JedisConnectionException("Unexpected end of stream.");
				}
			} catch (IOException e) {
				throw new JedisConnectionException(e);
			}
		}
	}

	public static byte[] encode(String param) throws UnsupportedEncodingException {
		if (param != null) {
			return param.getBytes("UTF-8");
		}
		return null;
	}

	public static String encode(byte[] data) throws UnsupportedEncodingException {
		if (data != null) {
			return new String(data, "UTF-8");
		}
		return null;
	}

	public static void write(OutputStream os, byte b) throws IOException {
		if (count == buf.length) {
			flushBuffer(os);
		}
		buf[count++] = b;
	}

	public static void write(OutputStream os, byte[] b) throws IOException {
		write(os, b, 0, b.length);
	}

	public static void write(OutputStream os, final byte[] b, final int off, final int len) throws IOException {
		if (len >= buf.length) {
			flushBuffer(os);
			os.write(b, off, len);
		} else {
			if (len >= buf.length - count) {
				flushBuffer(os);
			}

			System.arraycopy(b, off, buf, count, len);
			count += len;
		}
	}

	private static void flushBuffer(OutputStream os) throws IOException {
		if (count > 0) {
			os.write(buf, 0, count);
			count = 0;
		}
	}

	public static void writeIntCrLf(OutputStream os, int value) throws IOException {
		if (value < 0) {
			write(os, (byte) '-');
			value = -value;
		}

		int size = 0;
		while (value > sizeTable[size])
			size++;

		size++;
		if (size >= buf.length - count) {
			flushBuffer(os);
		}

		int q, r;
		int charPos = count + size;

		while (value >= 65536) {
			q = value / 100;
			r = value - ((q << 6) + (q << 5) + (q << 2));
			value = q;
			buf[--charPos] = DigitOnes[r];
			buf[--charPos] = DigitTens[r];
		}

		for (;;) {
			q = (value * 52429) >>> (16 + 3);
			r = value - ((q << 3) + (q << 1));
			buf[--charPos] = digits[r];
			value = q;
			if (value == 0)
				break;
		}
		count += size;

		writeCrLf(os);
	}

	public static void writeCrLf(OutputStream os) throws IOException {
		if (2 >= buf.length - count) {
			flushBuffer(os);
		}

		buf[count++] = '\r';
		buf[count++] = '\n';
	}

	public static enum Command {
		PING, SET, GET, QUIT, EXISTS, DEL, TYPE, FLUSHDB, KEYS, RANDOMKEY, RENAME, RENAMENX, RENAMEX, DBSIZE, EXPIRE, EXPIREAT, TTL, SELECT, MOVE, FLUSHALL, GETSET, MGET, SETNX, SETEX, MSET, MSETNX, DECRBY, DECR, INCRBY, INCR, APPEND, SUBSTR, HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, RPOPLPUSH, SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD, SISMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE, SDIFF, SDIFFSTORE, SRANDMEMBER, ZADD, ZRANGE, ZREM, ZINCRBY, ZRANK, ZREVRANK, ZREVRANGE, ZCARD, ZSCORE, MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, BLPOP, BRPOP, AUTH, SUBSCRIBE, PUBLISH, UNSUBSCRIBE, PSUBSCRIBE, PUNSUBSCRIBE, PUBSUB, ZCOUNT, ZRANGEBYSCORE, ZREVRANGEBYSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZUNIONSTORE, ZINTERSTORE, ZLEXCOUNT, ZRANGEBYLEX, ZREVRANGEBYLEX, ZREMRANGEBYLEX, SAVE, BGSAVE, BGREWRITEAOF, LASTSAVE, SHUTDOWN, INFO, MONITOR, SLAVEOF, CONFIG, STRLEN, SYNC, LPUSHX, PERSIST, RPUSHX, ECHO, LINSERT, DEBUG, BRPOPLPUSH, SETBIT, GETBIT, BITPOS, SETRANGE, GETRANGE, EVAL, EVALSHA, SCRIPT, SLOWLOG, OBJECT, BITCOUNT, BITOP, SENTINEL, DUMP, RESTORE, PEXPIRE, PEXPIREAT, PTTL, INCRBYFLOAT, PSETEX, CLIENT, TIME, MIGRATE, HINCRBYFLOAT, SCAN, HSCAN, SSCAN, ZSCAN, WAIT, CLUSTER, ASKING, PFADD, PFCOUNT, PFMERGE, READONLY, GEOADD, GEODIST, GEOHASH, GEOPOS, GEORADIUS, GEORADIUSBYMEMBER, BITFIELD;
	}
}
