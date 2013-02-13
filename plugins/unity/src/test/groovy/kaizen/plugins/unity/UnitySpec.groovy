package kaizen.plugins.unity

import kaizen.commons.Paths
import kaizen.plugins.unity.internal.MonoFramework
import org.gradle.internal.os.OperatingSystem
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class UnitySpec extends Specification {

	@Unroll('executable for #operatingSystem is #executable')
	def 'executable resolved against location when set'() {

		given:
		def unity = new Unity(null, operatingSystem)

		when:
		unity.location = '../unity'

		then:
		unity.executable == Paths.combine('../unity', executable)

		where:
		operatingSystem | executable
		windows()       | 'Unity.exe'
		osx()           | 'Contents/MacOS/Unity'
		linux()         | 'Unity'
	}

	def 'executable resolved against locator when location is not set'() {
		given:
		def unityLocation = 'Unity.app'
		def unityLocator = Mock(UnityLocator)
		def unity = new Unity(unityLocator, operatingSystem)

		when:
		def unityExecutable = unity.executable

		then:
		1 * unityLocator.getUnityLocation() >> unityLocation
		unityExecutable == Paths.combine(unityLocation, executable)

		where:
		operatingSystem | executable
		windows()       | 'Unity.exe'
		osx()           | 'Contents/MacOS/Unity'
		linux()         | 'Unity'
	}

	def 'UnityPlugin installs unity as clr provider'() {
		given:
		def project = ProjectBuilder.newInstance().build()

		when:
		project.plugins.apply(UnityPlugin)

		then:
		project.extensions.clr.providers.contains(project.extensions.unity)
	}

	@Unroll("clr location on #operatingSystem is #cli")
	def 'Unity clr is MonoBleedingEdge'() {
		given:
		def unityLocation = 'Unity.app'
		def unity = new Unity({ unityLocation} as UnityLocator, operatingSystem)

		when:
		def clr = unity.runtimeForFrameworkVersion('v3.5')

		then:
		(clr as MonoFramework).cli == Paths.combine(unityLocation, cli)

		where:
		operatingSystem | cli
		windows()       | 'Data/MonoBleedingEdge/bin/cli.bat'
		osx()           | 'Contents/Frameworks/MonoBleedingEdge/bin/cli'
		linux()         | 'Data/MonoBleedingEdge/bin/cli'
	}

	def windows() {
		Mock(OperatingSystem) {
			1 * isWindows() >> true
			_ * getScriptName(_) >> { args -> "${args[0]}.bat" }
			_ * toString() >> 'windows'
		}
	}

	def osx() {
		Mock(OperatingSystem) {
			1 * isMacOsX() >> true
			_ * getScriptName(_) >> { args -> args[0] }
			_ * toString() >> 'osx'
		}
	}

	def linux() {
		Mock(OperatingSystem) {
			1 * isLinux() >> true
			_ * getScriptName(_) >> { args -> args[0] }
			_ * toString() >> 'linux'
		}
	}
}