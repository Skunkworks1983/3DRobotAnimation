package com.pi.robot.demo;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class ShaderLoader {
	public static int loadShaderPair() {
		int shaderProgram = GL20.glCreateProgram();
		int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		StringBuilder vertexShaderSource = new StringBuilder();
		vertexShaderSource
				.append("#version 120\n"
						+ "varying vec4 varyingColour;\n"
						+ "varying vec3 varyingNormal;\n"
						+ "varying vec4 varyingVertex;\n"
						+ "\n"
						+ "void main() {\n"
						+ "    varyingColour = gl_Color;\n"
						+ "    varyingNormal = gl_Normal;\n"
						+ "    varyingVertex = gl_Vertex;\n"
						+ "    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n"
						+ "}");
		StringBuilder fragmentShaderSource = new StringBuilder();
		fragmentShaderSource
				.append("#version 120\n"
						+ "varying vec4 varyingColour;\n"
						+ "varying vec3 varyingNormal;\n"
						+ "varying vec4 varyingVertex;\n"
						+ "\n"
						+ "void main() {\n"
						+ "    vec3 vertexPosition = (gl_ModelViewMatrix * varyingVertex).xyz;\n"
						+ "    vec3 surfaceNormal = normalize((gl_NormalMatrix * varyingNormal).xyz);\n"
						+ "    gl_FragColor.rgb = vec3(0,0,0);\n"
						+ "    gl_FragColor += gl_LightModel.ambient;\n"
						+ "    for (int i = 0; i<2; i++) {"
						+ "    vec3 lightDirection = normalize(gl_LightSource[i].position.xyz - vertexPosition);\n"
						+ "    float diffuseLightIntensity = max(0, dot(surfaceNormal, lightDirection));\n"
						+ "    gl_FragColor.rgb += diffuseLightIntensity * varyingColour.rgb;\n"
						+ "    vec3 reflectionDirection = normalize(reflect(-lightDirection, surfaceNormal));\n"
						+ "    float specular = max(0.0, dot(surfaceNormal, reflectionDirection));\n"
						+ "    if (diffuseLightIntensity != 0) {\n"
						+ "        float fspecular = pow(specular, gl_FrontMaterial.shininess);\n"
						+ "        gl_FragColor += fspecular;\n" + "    }\n"
						+ "    }\n" + "}");

		GL20.glShaderSource(vertexShader, vertexShaderSource);
		GL20.glCompileShader(vertexShader);
		if (GL20.glGetShaderi(vertexShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err
					.println("Vertex shader wasn't able to be compiled correctly. Error log:");
			System.err.println(GL20.glGetShaderInfoLog(vertexShader, 1024));
			return -1;
		}
		GL20.glShaderSource(fragmentShader, fragmentShaderSource);
		GL20.glCompileShader(fragmentShader);
		if (GL20.glGetShaderi(fragmentShader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err
					.println("Fragment shader wasn't able to be compiled correctly. Error log:");
			System.err.println(GL20.glGetShaderInfoLog(fragmentShader, 1024));
		}
		GL20.glAttachShader(shaderProgram, vertexShader);
		GL20.glAttachShader(shaderProgram, fragmentShader);
		GL20.glLinkProgram(shaderProgram);
		if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Shader program wasn't linked correctly.");
			System.err.println(GL20.glGetProgramInfoLog(shaderProgram, 1024));
			return -1;
		}
		GL20.glDeleteShader(vertexShader);
		GL20.glDeleteShader(fragmentShader);
		return shaderProgram;
	}
}
