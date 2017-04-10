/*

 * Copyright 2016-2017 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Test;

import cito.annotation.PathParam;
import cito.annotation.PathParamLiteral;
import cito.event.DestinationChanged;
import cito.event.Message;
import cito.stomp.Frame;


/**
 * Unit tests for {@link PathParamProducer}.
 * 
 * @author Daniel Siviter
 * @since v1.0 [14 Apr 2017]
 */
public class PathParamProducerTest {
	@Test
	public void set_path() {
		final PathParser parser = mock(PathParser.class);

		final QuietClosable close = PathParamProducer.set(parser);

		assertSame(parser, PathParamProducer.pathParser());

		close.close();

		assertNull(ReflectionUtil.get(PathParamProducer.class, "HOLDER", ThreadLocal.class).get());

		verifyNoMoreInteractions(parser);
	}

	@Test
	public void set_pathParser() {
		final QuietClosable close = PathParamProducer.set("/");

		assertNotNull(PathParamProducer.pathParser());

		close.close();

		assertNull(ReflectionUtil.get(PathParamProducer.class, "HOLDER", ThreadLocal.class).get());
	}

	@Test
	public void pathParser_path() {
		final PathParser parser = PathParamProducer.pathParser("/");
		final PathParser parser0 = PathParamProducer.pathParser("/");
		assertSame(parser, parser0);
	}

	@Test
	public void pathParam() {
		final InjectionPoint ip = mock(InjectionPoint.class);
		final PathParser parser = new PathParser("/{there}");
		final Message msg = mock(Message.class);
		final DestinationChanged dc = mock(DestinationChanged.class);
		final Frame frame = mock(Frame.class);
		when(msg.frame()).thenReturn(frame);
		when(frame.destination()).thenReturn("/here");
		final Annotated annotated = mock(Annotated.class);
		when(ip.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(PathParam.class)).thenReturn(new PathParamLiteral("there"));

		final String param = PathParamProducer.pathParam(ip, parser, msg, dc);
		assertEquals("here", param);

		verify(msg).frame();
		verify(frame).destination();
		verify(ip).getAnnotated();
		verify(annotated).getAnnotation(PathParam.class);
		verifyNoMoreInteractions(ip, msg, dc, frame, annotated);
	}

	@Test
	public void pathParam_message() {
		final InjectionPoint ip = mock(InjectionPoint.class);
		final PathParser parser = new PathParser("/{there}");
		final Message msg = mock(Message.class);
		final Frame frame = mock(Frame.class);
		when(msg.frame()).thenReturn(frame);
		when(frame.destination()).thenReturn("/here");
		final Annotated annotated = mock(Annotated.class);
		when(ip.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(PathParam.class)).thenReturn(new PathParamLiteral("there"));

		final String param = PathParamProducer.pathParam(ip, parser, msg, null);
		assertEquals("here", param);

		verify(msg).frame();
		verify(frame).destination();
		verify(ip).getAnnotated();
		verify(annotated).getAnnotation(PathParam.class);
		verifyNoMoreInteractions(ip, msg, frame, annotated);
	}

	@Test
	public void pathParam_destination() {
		final InjectionPoint ip = mock(InjectionPoint.class);
		final PathParser parser = new PathParser("/{there}");
		final DestinationChanged dc = mock(DestinationChanged.class);
		when(dc.getDestination()).thenReturn("/here");
		final Annotated annotated = mock(Annotated.class);
		when(ip.getAnnotated()).thenReturn(annotated);
		when(annotated.getAnnotation(PathParam.class)).thenReturn(new PathParamLiteral("there"));

		final String param = PathParamProducer.pathParam(ip, parser, null, dc);
		assertEquals("here", param);

		verify(dc).getDestination();
		verify(ip).getAnnotated();
		verify(annotated).getAnnotation(PathParam.class);
		verifyNoMoreInteractions(ip, dc, annotated);
	}
}
