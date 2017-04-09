# Citō Bill of Materials

Use this to cleanly ensure all the correct versions of Citō are imported into Maven. Usage:

	<dependencyManagement>
		<dependencies>
			...
			<dependency>
				<groupId>io.cito</groupId>
				<artifactId>bom</artifactId>
				<version>x.x.x</version>
				<scope>import</scope>
				<type>pom</type>
			</dependency>
		</dependencies>
	</dependencyManagement>