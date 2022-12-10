export class CredentialsDto {
	constructor(public username: string, public password: string) {}
}

export class TokensDto {
	constructor(public accessToken: string) {}
}
