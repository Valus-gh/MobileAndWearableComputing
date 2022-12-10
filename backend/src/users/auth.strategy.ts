import { Strategy, ExtractJwt } from 'passport-jwt';
import { PassportStrategy } from '@nestjs/passport';
import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { UsersService } from './users.service';
import { User } from './users.entities';

@Injectable()
export class JwtAuthStrategy extends PassportStrategy(Strategy) {
	constructor(
		private userService: UsersService,
		configService: ConfigService,
	) {
		super({
			jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
			secretOrKey: configService.getOrThrow('JWT_SECRET'),
		});
	}

	async validate(jwtPayload: object & { sub: string }): Promise<User> {
		try {
			const user = await this.userService.validateJWT(jwtPayload);
			if (!user) {
				throw new UnauthorizedException();
			}
			return user;
		} catch (e) {
			throw new UnauthorizedException();
		}
	}
}
